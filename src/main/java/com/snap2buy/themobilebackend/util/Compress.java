package com.snap2buy.themobilebackend.util;

/**
 * Created by sachin on 3/31/16.
 */
public class Compress {


    public static void main(String args[]) {

//      System.out.println("0="+args[0]);
//      System.out.println("1="+args[1]);
//      System.out.println("2="+args[2]);
//      System.out.println("3="+args[3]);

        String filePath = args[0];
        String thumbnailPath = args[1];
        Double imageRotation = Double.valueOf(args[2]);
        Integer compressSize = Integer.valueOf(args[3]);

//        javaxt.io.Image image = new javaxt.io.Image(filePath);
//        //System.out.println("original image::" + image.getWidth() + "x" + image.getHeight());
//
//        image.rotate(imageRotation);
//        String origHeight = String.valueOf(image.getHeight());
//        String origWidth = String.valueOf(image.getWidth());
//        image.setWidth(compressSize);
//
//        //System.out.println("new image::" + image.getWidth() + "x" + image.getHeight());
//        String newWidth = String.valueOf(image.getWidth());
//        String newHeight = String.valueOf(image.getHeight());
//
//        image.saveAs(thumbnailPath);
//        System.out.println(origWidth+","+origHeight+","+newWidth+","+newHeight);
    }
}
