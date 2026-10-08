package com.payflow.merchantservice.utils;


public final class FuzzyMatchUtil {


    public FuzzyMatchUtil() {
    }

    public static double calculateSimilarity(String s1 , String s2){
        if (s1==null || s2== null)return 0.0;

        String clean1 =s1.trim().toUpperCase().replaceAll("[^A-Z0-9]" ,"");
        String clean2 =s2.trim().toUpperCase().replaceAll("[^A-Z0-9]" ,"");

        if (clean1.equals(clean2))return 1.0;

        if (clean1.isEmpty() || clean2.isEmpty())return 0.0;

        int [] mtp = matches(clean1 , clean2);

        float m = mtp[0];

        if (m == 0)return 0.0;

        float j = (m/clean1.length() +m / clean2.length() + (m -mtp[1])/m)/3;

        float p = 0.1f;

        int l = Math.max(4 ,mtp[2]);


        double k =  j + (l*p *(1-j));
        System.out.println(k);
         return k;
    }

    private static int[] matches(String s1, String s2) {
        int maxDist = Math.max(s1.length(), s2.length()) / 2 - 1;
        boolean[] matched1 = new boolean[s1.length()];
        boolean[] matched2 = new boolean[s2.length()];
        int matches = 0;
        for (int i = 0; i < s1.length(); i++) {
            int start = Math.max(0, i - maxDist);
            int end = Math.min(i + maxDist + 1, s2.length());
            for (int j = start; j < end; j++) {
                if (!matched2[j] && s1.charAt(i) == s2.charAt(j)) {
                    matched1[i] = true;
                    matched2[j] = true;
                    matches++;
                    break;
                }
            }
        }
        if (matches == 0) return new int[]{0, 0, 0};
        int transpositions = 0, k = 0;
        for (int i = 0; i < s1.length(); i++) {
            if (!matched1[i]) continue;
            while (!matched2[k]) k++;
            if (s1.charAt(i) != s2.charAt(k)) transpositions++;
            k++;
        }
        int prefix = 0;
        for (int i = 0; i < Math.min(s1.length(), s2.length()); i++) {
            if (s1.charAt(i) == s2.charAt(i)) prefix++;
            else break;
        }
        return new int[]{matches, transpositions / 2, prefix};
    }
}
