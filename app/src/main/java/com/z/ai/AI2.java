package com.z.ai;

/**
 * Created by rz on 15/5/10.
 */
public class AI2 {


    static {
        System.loadLibrary("ai2");
    }

    public static native int getAIResult(String board);

    private static int[] dirUni = {0, 2, 3, 1};

    public static int getUniformAIResult(String board) {
        int ret = getAIResult(board);
        if (ret >= 0 && ret <= 3) {
            return dirUni[ret];
        }
        return ret;
    }

}
