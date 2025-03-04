package com.mirrar.tablettryon;

import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        // Your main method logic here

        System.out.println(getLargestPrime(217));
    }

    public static boolean isPerfectNumber(int number) {
        if (number < 1) {
            return false;
        }

        int returnValue = 0;
//        ArrayList l = new ArrayList<Integer>();
        int[] l = new int[number];

        for (int i = 1; i < number; i++) {
            if (number % i == 0) {
                l[i] = i;
            }
        }

        for (int j : l) {
            returnValue += j;
        }

        return returnValue == number;
    }


    public static void numberToWords(int number) {
        String[] nameArray = new String[]{"Zero", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine"};

        if (number < 0) {
            System.out.println("Invalid Value");
        }

        String nums = String.valueOf(number);

        for (int i = 0; i < nums.length(); i++) {
            int index = nums.charAt(i) - '0';
            System.out.println(nameArray[index]);
        }
    }

    public static boolean canPack(int bigCount, int smallCount, int goal) {
        if (bigCount < 0 || smallCount < 0) {
            return false;
        }

        boolean rv = false;
        for (int i = 0; i <= bigCount; i++) {
            if ((i * 5) == goal) {
                rv = true;
                break;
            } else {
                for (int j = 0; j <= smallCount; j++) {
                    if (j == goal) {
                        rv = true;
                        break;
                    } else {
                        if (i * 5 + j == goal) {
                            rv = true;
                            break;
                        }
                    }
                }
            }
        }
        return rv;
    }

    public static int getLargestPrime(int number) {
        if (number < 1) {
            return -1;
        }

        int primeCount = 1;

        for (int i = 2; i< number; i++) {

            if (number % i == 0) {
                boolean ip = isPrime(i);

                if (ip) {
                    primeCount = i;
                }
            }
        }

        return primeCount;
    }

    public static boolean isPrime(int n) {
        for (int i = 2; i<n; i++) {
            if (n % i == 0) {
                return false;
            }
        }

        return true;
    }

    public static boolean canPack2(int bigCount, int smallCount, int goal) {
        if (bigCount < 0 || smallCount < 0) {
            return false;
        }

        int bigBags = Math.min(goal / 5, bigCount);
        int rem = goal - (bigBags * 5);

        return rem <= smallCount;
    }

    public static int reverse(int number) {
        if (number < 0) {
            return -1;
        } else {
            return Integer.parseInt(new StringBuilder(String.valueOf(number)).reverse().toString());
        }
    }

    public static int getDigitCount(int number) {
        if (number < 0) {
            return -1;
        } else {
            return String.valueOf(number).length();
        }
    }

    public static boolean isPerfectNumber2(int number) {
        if (number <= 1) {
            return false; // Perfect numbers are greater than 1
        }

        int sum = 1;  // Start with 1, because 1 is a divisor of any number

        // Check for divisors up to the square root of the number
        for (int i = 2; i <= Math.sqrt(number); i++) {
            if (number % i == 0) {
                sum += i; // Add the divisor
                if (i != number / i) {
                    sum += number / i; // Add the corresponding pair divisor
                }
            }
        }

        return sum == number; // Check if the sum of divisors equals the number
    }

    public static boolean isValid(int first) {
        return first >= 10 && first <= 1000;
    }

    public static boolean hasSameLastDigit(int first, int second, int third) {

        boolean b = isValid(first);
        boolean bb = isValid(second);
        boolean bbb = isValid(third);


        if (b && bb && bbb) {
            String fS = String.valueOf(first);
            String SS = String.valueOf(second);
            String TT = String.valueOf(third);

            String fl = fS.charAt(fS.length() - 1) + "";
            String sl = SS.charAt(SS.length() - 1) + "";
            String tl = TT.charAt(TT.length() - 1) + "";

            return fl.equals(sl) || sl.equals(tl) || fl.equals(tl);
        } else {
            return false;
        }
    }


    public static boolean isPalindrome(int number) {

        String n = String.valueOf(Math.abs(number));

        int fstind = 0;
        int lstind = n.length() - 1;

        boolean value = true;
        while (fstind < lstind) {

            String f = String.valueOf(n.charAt(fstind));
            String l = String.valueOf(n.charAt(lstind));

            if (!f.equals(l)) {
                return false;
            }
            fstind++;
            lstind--;
        }

        return value;
    }


    public static boolean hasSharedDigit(int first, int second) {
        if (first < 10 || first > 99 || second < 10 || second > 99) {
            return false;
        }

        String fS = String.valueOf(first);
        String SS = String.valueOf(second);

        String f = fS.charAt(0) + "";
        String l = fS.charAt(fS.length() - 1) + "";

        return SS.contains(f) || SS.contains(l);
    }

    public static int sumFirstAndLastDigit(int number) {

        if (number < 0) {
            return -1;
        } else if (number < 10) {
            return number + number;
        }

        String n = String.valueOf(number);

        int fstind = 0;
        int lstind = n.length() - 1;

        int f = n.charAt(fstind) - '0';
        int l = n.charAt(lstind) - '0';

        int finalReturn = f + l;
        if (finalReturn > 9) {
            return sumFirstAndLastDigit(finalReturn);
        } else {
            return finalReturn;
        }
    }
}
