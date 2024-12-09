import com.google.common.base.Stopwatch;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BaseClass {
    public static final String RESULT_FILE="IMG_0903_2.jpg";
    public static void main(String[] args) throws IOException {
        BufferedImage originalImage= ImageIO.read(new File(BaseClass.class.getResource("IMG_0903.jpg").getFile()));
        BufferedImage resultImage= new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), BufferedImage.TYPE_INT_RGB);
        Stopwatch stopwatch = Stopwatch.createStarted();
        for (int x = 0; x < 10; x++) {
            recolorSingleThread(originalImage,resultImage);
        }
        stopwatch.stop();
        System.out.println("Single Thread :"+ stopwatch.elapsed());

        stopwatch = Stopwatch.createStarted();
        for (int x = 0; x < 10; x++) {
            recolorMultiThread(originalImage,resultImage,20);
        }
        stopwatch.stop();
        System.out.println("multi Thread :"+ stopwatch.elapsed());

        stopwatch = Stopwatch.createStarted();
        for (int x = 0; x < 10; x++) {
            recolorMultiVirtualThread(originalImage,resultImage,20);
        }
        stopwatch.stop();
        System.out.println("virtual multi Thread :"+ stopwatch.elapsed());

        File resultFile=new File(RESULT_FILE);
        ImageIO.write(resultImage, "jpg", resultFile);
    }
    public static void recolorSingleThread(BufferedImage originalImage,BufferedImage resultImage) {
        recolorImage(originalImage,resultImage,0,0,originalImage.getWidth(),originalImage.getHeight());
    }

    public static void recolorMultiThread(BufferedImage originalImage,BufferedImage resultImage,int threadCount) {
        List<Thread> threads=new ArrayList<>();
        int width=originalImage.getWidth();
        int height=originalImage.getHeight()/threadCount;
        for(int i=0;i<threadCount;i++) {
            final int threadMultiplier=i;
            threads.add(new Thread(()->{
                recolorImage(originalImage,resultImage,0,height*threadMultiplier,width,height);
            }));
        }
        for(Thread thread:threads) {
            thread.start();
        }
        for(Thread thread:threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void recolorMultiVirtualThread(BufferedImage originalImage,BufferedImage resultImage,int threadCount) {
        List<Thread> threads=new ArrayList<>();
        int width=originalImage.getWidth();
        int height=originalImage.getHeight()/threadCount;
        for(int i=0;i<threadCount;i++) {
            final int threadMultiplier=i;
            threads.add(Thread.startVirtualThread (()->{
                recolorImage(originalImage,resultImage,0,height*threadMultiplier,width,height);
            }));
        }

        for(Thread thread:threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }



    public static void recolorImage (BufferedImage originalImage,BufferedImage resultImage
            ,int leftCorner,int topCorner,int width,int height) {
        for(int x=leftCorner; x<leftCorner+width && x< originalImage.getWidth(); x++) {
            for(int y=topCorner; y<topCorner+height && y<originalImage.getHeight(); y++) {
                recolorPixel(originalImage,resultImage,x,y);
            }
        }
    }

    public static void recolorPixel(BufferedImage originalImage,BufferedImage resultImage,int x,int y){
        int rgb = originalImage.getRGB(x,y);
        int red=getRed(rgb);
        int green=getGreen(rgb);
        int blue=getBlue(rgb);

        int newRed,newGreen,newBlue;
        if(isShadeOfGray(red,green,blue)){
            newRed=Math.min(255,red+10);
            newGreen=Math.max(0,green-80);
            newBlue=Math.max(0,blue-20);
        }
        else {
            newRed=red;
            newGreen=green;
            newBlue=blue;
        }
        int newRgb=createRgbFromColor(newRed,newGreen,newBlue);
        setRgb(resultImage,x,y,newRgb);
    }
    public static void setRgb(BufferedImage image, int x,int y,int rgb){
        image.getRaster().setDataElements(x,y,image.getColorModel().getDataElements(rgb,null));
    }

    public static int getRed(int rgb){
        return (rgb & 0x00FF0000) >> 16;
    }
    public static int getGreen(int rgb){
        return (rgb & 0x0000FF00) >> 8;
    }
    public static int getBlue(int rgb){
        return (rgb & 0x000000FF);
    }
    public static int createRgbFromColor(int red, int green, int blue){
        int rgb=0;
        rgb |= (red<<16) | (green<<8) | blue;
        rgb |= 0xFF000000;
        return rgb;

    }

    public static boolean isShadeOfGray(int red, int green, int blue){
        return Math.abs(red-green)<30 && Math.abs(blue-green)<30 && Math.abs(red-blue)<30;
    }

}
