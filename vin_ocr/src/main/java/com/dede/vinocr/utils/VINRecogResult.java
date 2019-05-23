package com.dede.vinocr.utils;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by user on 2018/5/18.
 */

public class VINRecogResult implements Serializable {
    public ArrayList<String> savePath = new ArrayList<>();// 图片路径的集合
    public String[] httpContent;//上传服务的内容  包括图片路径  以及 识别结果
    public String result;
}
