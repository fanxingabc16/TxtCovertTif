package com.company;

import com.sun.media.imageio.plugins.tiff.*;
import com.sun.media.imageioimpl.plugins.tiff.TIFFImageWriter;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.util.Iterator;
import java.util.Locale;
import javax.imageio.*;
import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.FileImageOutputStream;


public class TxtConverTif {

    private static String METADATA_NAME = "com_sun_media_imageio_plugins_tiff_image_1.0";
    private static int DPI_X = 600;
    private static int DPI_Y = 600;
    public static void main(String[] args) throws Throwable {
        System.out.println("开始生成TIF图片......");
        Font font = new Font("Arial", Font.BOLD, 14*10);
        BufferedImage bufferedImage= TxtConverTif.drawTranslucentStringPic("This Card was printed with the",font,2100,1344);
        RenderedImage image = bufferedImage;
        Iterator writers = ImageIO.getImageWritersByFormatName("TIFF");
        if (writers == null || !writers.hasNext()) {
            throw new IllegalStateException("No TIFF writers!");
        }
        ImageWriter writer = (ImageWriter) writers.next();
        ImageTypeSpecifier imageType = ImageTypeSpecifier.createFromRenderedImage(image);
        IIOMetadata imageMetadata = writer.getDefaultImageMetadata(imageType, null);
        String fileName= "dpi_api.tif";
        imageMetadata = setDPIViaAPI(imageMetadata);
        writer.setOutput(new FileImageOutputStream(new File(fileName)));//设置文件保存路径
        TIFFImageWriteParam writeParams = new TIFFImageWriteParam(Locale.ENGLISH);
        writeParams.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        System.out.println("设置图片压缩编码 LZW.");
        writeParams.setCompressionType(getCompression(5));//压缩
        writeParams.setCompressionQuality(1f);
        writer.write(null,new IIOImage(image, null, imageMetadata),writeParams);
        System.out.println("TIF图片生成成功");
    }
    /**
     * 创建图片
     * @param content 内容
     * @param font  字体
     * @param width 宽
     * @param height 高
     * @return
     */
    private static BufferedImage createImage(String content, Font font, Integer width, Integer height){
        BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g2 = (Graphics2D)bi.getGraphics();
        g2.setBackground(Color.WHITE);
        g2.clearRect(0, 0, width, height);
        g2.setPaint(Color.BLACK);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB); //去除锯齿
        //g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); // 去除锯齿(背景)
        FontRenderContext context = g2.getFontRenderContext();
        Rectangle2D bounds = font.getStringBounds(content, context);
        double x = (width - bounds.getWidth()) / 2;
        double y = (height - bounds.getHeight()) / 2;
        double ascent = -bounds.getY();
        double baseY = y + ascent;
        g2.drawString(content, (int)x, (int)baseY);
        return bi;
    }

    public static BufferedImage drawTranslucentStringPic(String content,Font font ,int width, int height) {

        try {
            //设置白色底
            System.out.println("设置位深......");
            BufferedImage buffImg = new BufferedImage(width, height,BufferedImage.TYPE_BYTE_GRAY);
            Graphics2D gd = buffImg.createGraphics();
            //设置透明
           // buffImg = gd.getDeviceConfiguration().createCompatibleImage(width, height, Transparency.TRANSLUCENT);
            gd=(Graphics2D)buffImg.getGraphics();
            //设置透明  end
            gd.setFont(font); //设置字体
            //gd.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB); //消除锯齿状
            gd.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); // 去除锯齿(当设置的字体过大的时候,会出现锯齿)
            gd.setBackground(Color.white);
            gd.setColor(Color.black); //设置颜色
            gd.clearRect(0, 0, width, height);
            FontRenderContext context = gd.getFontRenderContext();
            Rectangle2D bounds = font.getStringBounds(content, context);
            double x = (width - bounds.getWidth()) / 2;
            double y = (height - bounds.getHeight()) / 2;
            double ascent = -bounds.getY();
            double baseY = y + ascent;
            gd.drawString(content, (int)x, (int)baseY);
            return buffImg;
        } catch (Exception e) {
            return null;
        }
    }

    protected static String getCompression(final int compression) {
        String c = null;
        for (int i = 0; i < TIFFImageWriter.compressionTypes.length; ++i) {
            if (compression == TIFFImageWriter.compressionNumbers[i]) {
                c = TIFFImageWriter.compressionTypes[i];
            }
        }
        return c;
    }
    /**
     * Set DPI using API.
     */
    private static IIOMetadata setDPIViaAPI(IIOMetadata imageMetadata) throws IIOInvalidTreeException {
        // Derive the TIFFDirectory from the metadata.
        TIFFDirectory dir = TIFFDirectory.createFromMetadata(imageMetadata);
        BaselineTIFFTagSet base = BaselineTIFFTagSet.getInstance();
        TIFFTag tagXRes = base.getTag(BaselineTIFFTagSet.TAG_X_RESOLUTION);
        TIFFTag tagYRes = base.getTag(BaselineTIFFTagSet.TAG_Y_RESOLUTION);
        char[] cResolutionUnit = new char[] {BaselineTIFFTagSet.RESOLUTION_UNIT_INCH};//英寸
        TIFFTag tagResUnit = base.getTag(BaselineTIFFTagSet.TAG_RESOLUTION_UNIT);
        TIFFField fieldResUnit = new TIFFField(tagResUnit, TIFFTag.TIFF_SHORT, 1, cResolutionUnit);
        TIFFField fieldXRes = new TIFFField(tagXRes, TIFFTag.TIFF_RATIONAL, 1, new long[][]{{DPI_X, 1}});
        TIFFField fieldYRes = new TIFFField(tagYRes, TIFFTag.TIFF_RATIONAL, 1, new long[][]{{DPI_Y, 1}});
        // Append {X,Y}Resolution fields to directory.
        dir.addTIFFField(fieldXRes);
        dir.addTIFFField(fieldYRes);
        System.out.println("设置分辨率单位......");
        dir.addTIFFField(fieldResUnit);
        dir.addTIFFField(fieldYRes);
        return dir.getAsMetadata();
    }
}