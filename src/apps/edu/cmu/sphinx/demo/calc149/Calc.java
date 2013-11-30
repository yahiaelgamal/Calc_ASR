package edu.cmu.sphinx.demo.calc149;
/*
 * Copyright 1999-2004 Carnegie Mellon University.
 * Portions Copyright 2004 Sun Microsystems, Inc.
 * Portions Copyright 2004 Mitsubishi Electric Research Laboratories.
 * All Rights Reserved.  Use is subject to license terms.
 *
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL
 * WARRANTIES.
 *
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import sun.misc.Regexp;

import edu.cmu.sphinx.frontend.util.Microphone;
import edu.cmu.sphinx.recognizer.Recognizer;
import edu.cmu.sphinx.result.Result;
import edu.cmu.sphinx.util.props.ConfigurationManager;

public class Calc{

    public HashMap<String, Double> vars;

    // A processed array in which numbers are converted to a double
    // string representation ex. "120.0". Note that it is encoded to handle
    // doubles not integers (twelve will be "12.0" not "12")
    public String[] operands;
    // A string either {store, define, retrieve, operation}
    public String operation;
    // a raw string returned from the recognizer
    public String recognizedString;
    // A string representing the result (encoded as doubles)
    public String result;
    public String lastError;
    public boolean errorHappend;
    private Recognizer recognizer;
    private Microphone microphone;

    public Calc() {
        this(true);
    }
    public Calc(boolean mic) {
        if(mic) {
            ConfigurationManager cm;
            cm = new ConfigurationManager(Calc.class.getResource("calc.config.xml"));

            this.recognizer = (Recognizer) cm.lookup("recognizer");
            recognizer.allocate();

            this.microphone = (Microphone) cm.lookup("microphone");
        }
        this.vars = new HashMap<String, Double>();
        vars.put("pie", Math.PI);
        vars.put("e", Math.E);
    }

    public void listenOnce() {
        listenOnce(false);
    }

    public void listenOnce(boolean text) {
        this.errorHappend = false;
        if(text) {
            System.out.println("TYPE something to start. Press Ctrl-C to quit.\n");
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            try {
                routeToHandler(br.readLine(), false);
            } catch (IOException e) {
                lastError = e.getMessage();
                e.printStackTrace();
            }
        } else {
            this.microphone.clear();
            this.microphone.startRecording();

            System.out.println("Say something to start. Press Ctrl-C to quit.\n");
            Result result = recognizer.recognize();

            if (result != null) {
                String resultText = result.getBestFinalResultNoFiller();
                System.out.println("You said: " + resultText + '\n');
                this.routeToHandler(resultText, false);
            } else {
                errorHappend = true;
                lastError = "I can't hear what you said.\n";
                System.err.println("I can't hear what you said.\n");
            }
        }
    }

    public void doTextStuff(String s){
        routeToHandler(s, true);
    }

    public void stop() {
        this.microphone.stopRecording();
        this.recognizer.deallocate();
    }

    public String toString() {
        String s = "";
        s += "Operation " + this.operation;
        s += "\nOperands " + Arrays.toString(this.operands);
        s += "\nResult " + this.result;
        return s;
    }

    public void routeToHandler(String s, boolean gui) {
        this.recognizedString = s;
        String operation = "";
        if(gui){
            operation = getOperationSymbole(s);
        }else{
            operation = getOperation(s);
        }
        this.operation =  operation;
        String[] operations = {"plus", "minus", "times", "power",  "over",
                "log", "sin", "cos", "tan", "+", "-", "/", "*", "^"};

        Arrays.sort(operations);

        if(operation.equals("store")) {
            handleStore(s);
        } else if(operation.equals("define")) {
            handleDefine(s);
        } else if(operation.equals("retrieve")) {
            handleRetrieve(s);
        } else if(Arrays.binarySearch(operations, operation) >= 0) {
            handleOperation(s);
        } else {
//          this.result = buildNumber(s) + "";
            this.errorHappend = true;
            lastError = "No operation caught";
            System.err.println("No operation caught");
        }
    }

    private void handleStore(String s) {
        if(s.indexOf("last result") != -1) {
            vars.put("temp", Double.parseDouble(this.result));
            this.operands = new String[] {"last result"};
        } else {
            String[] splitted = s.split(" ", 3);
            this.operands = new String[] {splitted[splitted.length-2],
                    buildNumber(splitted[splitted.length-1]) + ""};
            this.vars.put(this.operands[0], Double.parseDouble(this.operands[1]));
            this.result = this.operands[1];
        }
    }

    private void handleDefine(String s) {
        String[] splitted = s.split(" ");
        this.operands = new String[] {splitted[splitted.length-1]};
        this.vars.put(s, 0.0);
    }

    private void handleRetrieve(String s) {
        this.operands = new String[] {} ;
        if(s.indexOf("last result") == -1) {
            String var = s.split(" ",2)[1];
            this.result = this.vars.get(var) + "";
        }
    }

    private void handleOperation(String s) {
        String[] sarr = s.split("\\s*(plus|minus|over|times|power|log|sine|sin|cos|tan|\\+|\\-|/|\\*)\\s*");
//        System.out.println(Arrays.toString(sarr));
        if(sarr[0].equals("")) // speical case of <operator> <operand>
            sarr  = new String[] {sarr[1]};


        if(sarr.length == 3) { // BRACE YOURSELVES, A LOT OF HACKYNESS WILL HAPPEN
            Pattern ops = Pattern.compile("(plus|minus|over|times|power|log|sine|sin|cos|tan|\\+|\\-|/|\\*)");
            Matcher matcher = ops.matcher(s);
            matcher.find();
            String firstOp = s.substring(matcher.start(), matcher.end()+1);
            matcher.find();
            String secondOp = s.substring(matcher.start(), matcher.end()+1);

            String[] newarr = new String[sarr.length];
            for(int i = 0; i < sarr.length; i++) {
                newarr[i] = buildNumber(sarr[i].trim()) + "";
            }

            if(isHigherPrescedence(firstOp, secondOp)) {
                this.operands = new String[] {newarr[0], newarr[1]};
                this.operation = firstOp.trim();
                printA(operands);
                this.makeOperation();
                operands[0] = result;
                operands[1] = newarr[2];
                this.operation = secondOp.trim();
                this.makeOperation();
            }else {
                this.operands = new String[] {newarr[1], newarr[2]};
                this.operation = secondOp.trim();
                this.makeOperation();
                operands[1] = this.result;
                operands[0] = newarr[0];
                this.operation = firstOp.trim();
                this.makeOperation();
            }
        }else {
            String[] newarr = new String[sarr.length];
            for(int i = 0; i < sarr.length; i++) {
                newarr[i] = buildNumber(sarr[i].trim()) + "";
            }
            this.operands = newarr;
            this.makeOperation();
        }

    }

    private void makeOperation() {
        double op1 = 0.0;
        double op2 = 0.0;
        if(isDoubleParsable(this.operands[0]))
            op1 = Double.parseDouble(this.operands[0]);
        else
            op1 = vars.get(this.operands[0]);

        if(!(operation.equals("log") || operation.equals("sin")
                || operation.equals("cos") || operation.equals("tan"))) {
            if(isDoubleParsable(this.operands[1]))
                op2 = Double.parseDouble(this.operands[1]);
            else
                op2 = vars.get(this.operands[1]);
        }

        if(this.operation.equals("plus") || this.operation.equals("+"))
            this.result = (op1 + op2) + "";
        else if(this.operation.equals("minus") || this.operation.equals("-"))
            this.result = (op1 - op2) + "";
        else if(this.operation.equals("times") || this.operation.equals("*"))
            this.result = (op1 * op2) + "";
        else if(this.operation.equals("power") || this.operation.equals("^"))
            this.result = Math.pow(op1, op2) + "";
        else if(this.operation.equals("over") || this.operation.equals("/"))
            this.result = (op1 / op2) + "";
        else if(this.operation.equals("log")) {
            this.result = Math.log10(op1) + "";
        }
//        else if(this.operation.equals("cos"))
//            this.result = Math.cos(op1*Math.PI/180) + "";
//        else if(this.operation.equals("sin"))
//            this.result = Math.sin(op1*Math.PI/180) + "";
//        else if(this.operation.equals("tan"))
//            this.result = Math.tan(op1*Math.PI/180) + "";
        else {
            this.errorHappend = true;
            this.lastError = "Operation non recognzied " + this.operation;
            System.err.println("Operation non recognzied " + this.operation);
        }

    }

    private double buildNumber(String str) {
        String resArr[] = str.split(" ");
        resArr = removeAnds(resArr);
        boolean allDigits = true;
        for(String s : resArr){
            if(!isDigit(s)) {
                allDigits = false;
                break;
            }
        }

        double number = 0;
        if(allDigits) {
            String resString = "";
            for(String s : resArr) {
                resString += (int) translateOneWord(s);
            }
            number = Double.parseDouble(resString);
        }else {
            number = handleWithSuffix(resArr);
        }
        return number;
    }

    private double handleWithSuffix(String[] resArr) {
        ArrayList<Integer> suffixIndices = new ArrayList<Integer>();
        for(int i = 0; i < resArr.length; i++) {
            String s = resArr[i];
            if(isSuffix(s)) {
                suffixIndices.add(i);
            }
        }

        int number = 0;
        for(int i = 0; i < suffixIndices.size(); i++) {
            int suffixIndex = suffixIndices.get(i);
            int lastSuffixIndex = -1;
            if(i != 0)
                 lastSuffixIndex = suffixIndices.get(i-1);

            int wordsBetweenSuffix = suffixIndex - lastSuffixIndex;
            String[] words = null;
            switch (wordsBetweenSuffix) {
            case 2:
                words = new String[] {resArr[suffixIndex-1]};
                break;
            case 3:
                words = new String[] {resArr[suffixIndex-2], resArr[suffixIndex-1]};
                break;
            default:
                errorHappend = true;
                lastError = "#words between suffix is not 2, 3";
                System.err.println("words between suffix is not 2, 3");
            }
            double beforeSuffix = translate(words);
            boolean isHundred = resArr[suffixIndex].equals("hundred") ||
                    resArr[suffixIndex].equals("hundreds");
            int  factor = isHundred  ? 100 : 1000;
            number += beforeSuffix * factor;
        }

        int lastSuffixIndex  = suffixIndices.size() != 0? suffixIndices.get(suffixIndices.size()-1) + 1: 0;
        int remainingWordsCount = resArr.length - lastSuffixIndex;
        double remainingValue = 0.0;
        String[] remainingWords = {};
        switch (remainingWordsCount) {
        case 1:
            remainingWords = new String[] {resArr[resArr.length - 1]};
            remainingValue += translate(remainingWords);
            break;
        case 2:
            remainingWords = new String[] {resArr[resArr.length - 2], resArr[resArr.length - 1]};
            remainingValue += translate(remainingWords);
            break;
        default:
            break;
        }
        return number + remainingValue;
    }

    private double translate(String[] words) {
        switch (words.length) {
        case 1:
            return translateOneWord(words[0]);
        case 2:
            return translateTwoWords(words[0], words[1]);
        default:
            System.err.println("WORDS IS OF LENGTH " + words.length );
            return -1;
        }
    }

    private double translateTwoWords(String word1, String word2) {
        return  translateOneWord(word1) + translateOneWord(word2);
    }

    private double translateOneWord(String str) {
        if(isNumParsable(str))
            return Double.parseDouble(str);
        else if(str.equals("one"))
            return 1;
        else if(str.equals("two"))
            return 2;
        else if(str.equals("three"))
            return 3;
        else if(str.equals("four"))
            return 4;
        else if(str.equals("five"))
            return 5;
        else if(str.equals("six"))
            return 6;
        else if(str.equals("seven"))
            return 7;
        else if(str.equals("eight"))
            return 8;
        else if(str.equals("nine"))
            return 9;
        else if(str.equals("zero"))
            return 0;
        else if(str.equals("eleven"))
            return 11;
        else if(str.equals("twelve"))
            return 12;
        else if(str.equals("thirteen"))
            return 13;
        else if(str.equals("fourteen"))
            return 14;
        else if(str.equals("fifteen"))
            return 15;
        else if(str.equals("sixteen"))
            return 16;
        else if(str.equals("seventeen"))
            return 17;
        else if(str.equals("eighteen"))
            return 18;
        else if(str.equals("nineteen"))
            return 19;
        else if(str.equals("ten"))
            return 10;
        else if(str.equals("twenty"))
            return 20;
        else if(str.equals("thirty"))
            return 30;
        else if(str.equals("fourty"))
            return 40;
        else if(str.equals("fifty"))
            return 50;
        else if(str.equals("sixty"))
            return 60;
        else if(str.equals("seventy"))
            return 70;
        else if(str.equals("eighty"))
            return 80;
        else if(str.equals("ninety"))
            return 90;
        else if(this.vars.containsKey(str)) {
            return this.vars.get(str);
        }
        else {
            lastError = "non recognized number " + str;
            errorHappend = true;
            System.err.println("non recognized number " + str);
            return 0;
        }
    }

    private static boolean isSuffix(String str) {
        return str.equals("hundred") || str.equals("thousand");
    }

    private static boolean isDigit(String str) {
        String[] digits  = {"one", "two", "three", "four", "five", "six", "seven",
                "eight", "nine", "zero"};
        for(String digit : digits)
            if(digit.equals(str))
                return true;
        return false;
    }

    private String getOperationSymbole(String s) {
        String [] symbols = {"+","-","/","*","^","log","sin","cos", "tan"};
        for(String sym: symbols){
            if(s.indexOf(sym) > -1)
                return sym;
        }
        errorHappend = true;
        lastError = "Sym Cannot find operation for string " + s;
        System.err.println("Sym Cannot find operation for string " + s);
        return "-1";
    }

    private String getOperation(String s) {
        if(s.startsWith("store"))
            return "store";
        else if(s.startsWith("define"))
            return "define";
        else if(s.startsWith("retrieve"))
            return "retrieve";
        else if(s.indexOf("plus") != -1)
            return "plus";
        else if(s.indexOf("minus") != -1)
            return "minus";
        else if(s.indexOf("over") != -1)
            return "over";
        else if(s.indexOf("times") != -1)
            return "times";
        else if(s.indexOf("log") != -1)
            return "log";
        else if(s.indexOf("sine") != -1)
            return "sin";
        else if(s.indexOf("cos") != -1)
            return "cos";
        else if(s.indexOf("power") != -1) // ORDER MATTERS
            return "power";
        else {
            errorHappend = true;
            lastError = "Cannot find operation for string " + s;
            System.err.println("Cannot find operation for string " + s);
            return "-1";
        }
    }

    public static String[] removeAnds(String[] arr) {
        int andCount = 0;
        for(String s : arr) {
            if(s.equals("and"))
                andCount +=1;
        }

        String[] newArr = new String[arr.length-andCount];
        int lastIndex = 0;
        for(int i = 0; i < arr.length; i++) {
            if(!arr[i].equals("and")) {
                newArr[lastIndex] = arr[i];
                lastIndex += 1;
            }
        }
        return newArr;
    }

    private static boolean isDoubleParsable(String s) {
        return s.matches("[0-9]+(\\.[0-9]+)?");
    }

    private static boolean isNumParsable(String s){
        return (s.matches("\\s*[0-9]+\\s*") || s.matches("\\s*[0-9]+(\\.[0-9]+)?\\s*"));
    }

    public static void getOps(String s) {
        String[] sarr = s.split("\\s*(plus|minus|over|times|power|log|sine|sin|cos|tan|\\+|\\-|/|\\*)\\s*");
    }

    public static void test() {
        Calc calc = new Calc(false);
        calc.routeToHandler("four thousand five hundred and sixty seven plus three thousand two hundred and fourteen", false);
        if(calc.result.equals("7781.0"))
            System.out.println("PASS 1");
        else
            System.out.println("FAIL 1: " + calc.result);

        calc.routeToHandler("two power twenty two", false);
        if(calc.result.equals("4194304.0"))
            System.out.println("PASS 2");
        else
            System.out.println("FAIL 2: " + calc.result);

        calc.routeToHandler("five hundred and sixty two times three thousand one hundred and two", false);
        if(calc.result.equals("1743324.0"))
            System.out.println("PASS 3");
        else
            System.out.println("FAIL 3: " + calc.result);

        calc.routeToHandler("fourty five minus twelve",false);
        if(calc.result.equals("33.0"))
            System.out.println("PASS 4");
        else
            System.out.println("FAIL 4: " + calc.result);

        calc.routeToHandler("fourty five minus twelve",false);
        if(calc.result.equals("33.0"))
            System.out.println("PASS 4");
        else
            System.out.println("FAIL 4: " + calc.result);

        calc.routeToHandler("log five thousand four hundred and ten",false);
        if(calc.result.equals("3.7331972651065692"))
            System.out.println("PASS 5");
        else
            System.out.println("FAIL 5: " + calc.result);

        calc.routeToHandler("e power one two",false);
        if(calc.result.equals("162754.79141900383"))
            System.out.println("PASS 6");
        else
            System.out.println("FAIL 6: " + calc.result);

    }

    public static void test2() {
        Calc calc = new Calc(false);
        calc.routeToHandler("store x twenty", false);
        calc.routeToHandler("store y fourty", false);
        calc.routeToHandler("x plus y", false);
        if(calc.result.equals("60.0"))
            System.out.println("PASS 1");
        else
            System.out.println("FAIL 1: " + calc.result);

        calc.routeToHandler("store x twenty", false);
        calc.routeToHandler("store y fourty", false);
        calc.routeToHandler("x plus y plus one thousand", false);
        if(calc.result.equals("1060.0"))
            System.out.println("PASS 2");
        else
            System.out.println("FAIL 2: " + calc.result);

        calc.routeToHandler("store x seventy", false);
        calc.routeToHandler("x minus fifty times e", false);
        if(calc.result.equals("-65.91409142295225"))
            System.out.println("PASS 3");
        else
            System.out.println("FAIL 3: " + calc.result);

        calc.routeToHandler("store x one two", false);
        calc.routeToHandler("store y three", false);
        calc.routeToHandler("x plus y power four", false);
        if(calc.result.equals("93.0"))
            System.out.println("PASS 4");
        else
            System.out.println("FAIL 4: " + calc.result);

        calc.routeToHandler("store r nine", false);
        calc.routeToHandler("pie times r power two", false);
        if(calc.result.indexOf("254.46900494") != -1)
            System.out.println("PASS 5");
        else
            System.out.println("FAIL 5: " + calc.result);

        calc.routeToHandler("pie plus twelve over  three", false);
        if(calc.result.indexOf("7.141592") != -1)
            System.out.println("PASS 6");
        else
            System.out.println("FAIL 6: " + calc.result);

    }
    public static boolean isHigherPrescedence(String op1, String op2) {
        int op1Value = getPrescedence(op1);
        int op2Value = getPrescedence(op2);
        return op1Value > op2Value;
    }
    public static int getPrescedence(String op) {
        op = op.trim();
        int opValue = 0;
        if(op.matches("(plus|minus|\\+|\\-)"))
            opValue = 1;
        if(op.matches("(times|over|\\*|\\/)"))
            opValue = 2;
        if(op.matches("(power|\\^)"))
            opValue = 3;
        return opValue;
    }
    public static void main(String[] args) throws IOException {
        test();
        test2();
    }
    public static void printA(Object[] arr) {
        System.out.println(Arrays.toString(arr));
    }
//    public static void main(String[] args) throws IOException {
//        Calc calc = new Calc();
//        while(true) {
//            calc.listenOnce();
//            System.out.println(calc);
//        }
//      //calc.stop();
//    }

}
