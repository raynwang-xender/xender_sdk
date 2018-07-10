package cn.xender.transfer;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.os.Handler;
import android.text.TextUtils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.google.zxing.qrcode.encoder.Encoder;
import com.google.zxing.qrcode.encoder.QRCode;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import cn.xender.core.log.Logger;

public class QrCodeCreateWorker {

    public static final int CREATE_SUCCESS_WHAT = 1987;
    private static BitmapCache cache;

    private int bgColor;

    public QrCodeCreateWorker() {

        if(cache == null){

            BitmapCache.BitmapCacheParams params = new BitmapCache.BitmapCacheParams();
            params.memCacheSize = 1024*2;//2M

            cache = new BitmapCache(params);
        }
    }

    public static Bitmap getQrBitmap(){
        return cache.getBitmapFromMemCache("hotsopt_qrcode");
    }

    public static void clear(){
        if(cache != null){
            cache.clearCache();
            cache = null;
        }
    }


    public void startWork(Context context,Handler _handler,String qrStr, int width, int height,int bgColor,boolean scoop){
        this.bgColor = bgColor;
        new Thread(new DoingTask(context,_handler,qrStr,width,height,scoop),"createHotspotQrCode").start();
    }

    class DoingTask implements Runnable{

        private String url;
        private int width,height;
        private Handler _handler;
        private boolean scoop;
        private Context context;

        DoingTask(Context context,Handler _handler,String url,int width,int height,boolean scoop){
            this.url = url;
            this.width =width;
            this.height = height;
            this._handler = _handler;
            this.scoop = scoop;
            this.context = context;
        }

        @Override
        public void run() {

            if(TextUtils.isEmpty(url)){
                return;
            }

            cache.clearCache();

            Bitmap addedLogoBitmap;
            if(scoop){
                int logoSize = PhonePxConversion.dip2px(context,30);
                Bitmap logo = getMyAppIcon(context,logoSize,logoSize);

                addedLogoBitmap = createQRCodeWithLogo(url,width,logo);
            }else{
                addedLogoBitmap = encodeAsBitmap(url,width,height);
            }


            if(addedLogoBitmap != null && !addedLogoBitmap.isRecycled()){
                cache.addBitmapToCache("hotsopt_qrcode",addedLogoBitmap);
                _handler.sendEmptyMessage(CREATE_SUCCESS_WHAT);
            }

        }
    }


    public static Bitmap getMyAppIcon(Context context, int width, int height)
    {
        try
        {
            PackageManager pManager = context.getPackageManager();

            Drawable dicon = pManager.getApplicationIcon(context.getPackageName());

            if(dicon instanceof BitmapDrawable){
                BitmapDrawable bd = (BitmapDrawable)dicon;

                Bitmap bitmap = bd.getBitmap();

                if(bitmap == null || bitmap.isRecycled()) return null;

                return  ThumbnailUtils.extractThumbnail(bitmap, width, height, 0);//这里的bitmap不需要释放
            }
            return drawableToBitmap(dicon,width,height);//ThumbnailUtils.extractThumbnail(drawableToBitmap(dicon),width,height,0);

        }
        catch (Exception e)
        {
        }catch(OutOfMemoryError e){
            if(Logger.r) Logger.e("out_memo", "------------" );
        }

        return null;
    }

    private static Bitmap drawableToBitmap(Drawable drawable,int width,int height) {

        Bitmap bitmap = Bitmap.createBitmap(

                width,

                height,

                drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888

                        : Bitmap.Config.RGB_565);

        Canvas canvas = new Canvas(bitmap);

        drawable.setBounds(0, 0, width, height);

        drawable.draw(canvas);

        return bitmap;

    }


    private Bitmap encodeAsBitmap(String str,int mWidth,int mHeight){
        Bitmap bitmap = null;
        BitMatrix result = null;
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
        hints.put(EncodeHintType.MARGIN, 0);//无边框
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
        try {
            result = multiFormatWriter.encode(str, BarcodeFormat.QR_CODE, mWidth, mHeight,hints);
            int w = result.getWidth();
            int h = result.getHeight();
            int[] pixels = new int[w * h];
            for (int y = 0; y < h; y++) {
                int offset = y * w;
                for (int x = 0; x < w; x++) {
                    pixels[offset + x] = result.get(x, y) ? Color.BLACK : bgColor;
                }
            }
            bitmap = Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels,0,w,0,0,w,h);
        } catch (WriterException e){
            e.printStackTrace();
        } catch (IllegalArgumentException iae){ // ?
            return null;
        }


        return bitmap;
    }

//    private Bitmap generateBitmap(String content,int width, int height) {
//        QRCodeWriter qrCodeWriter = new QRCodeWriter();
//        Map<EncodeHintType, Object> hints = new HashMap<>();
//        hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
//        hints.put(EncodeHintType.MARGIN, 0);//无边框
//        try {
//            BitMatrix encode = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, width, height, hints);
//            int[] pixels = new int[width * height];
//            for (int i = 0; i < height; i++) {
//                for (int j = 0; j < width; j++) {
//                    if (encode.get(j, i)) {
//                        pixels[i * width + j] = 0x00000000;
//                    } else {
//                        pixels[i * width + j] = 0xffffffff;
//                    }
//                }
//            }
//            return Bitmap.createBitmap(pixels, 0, width, width, height, Bitmap.Config.RGB_565);
//        } catch (WriterException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }


    private Bitmap addLogo(Bitmap qrBitmap, Bitmap logoBitmap) {
        int qrBitmapWidth = qrBitmap.getWidth();
        int qrBitmapHeight = qrBitmap.getHeight();
        int logoBitmapWidth = logoBitmap.getWidth();
        int logoBitmapHeight = logoBitmap.getHeight();
        Bitmap blankBitmap = Bitmap.createBitmap(qrBitmapWidth, qrBitmapHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(blankBitmap);
        canvas.drawBitmap(qrBitmap, 0, 0, null);
        canvas.save(Canvas.ALL_SAVE_FLAG);
//        float scaleSize = 1.0f;
//        while ((logoBitmapWidth / scaleSize) > (qrBitmapWidth / 3) || (logoBitmapHeight / scaleSize) > (qrBitmapHeight / 3)) {
//            scaleSize *= 2;
//        }
//        Logger.d("scaleSize",";"+scaleSize);
        float sx = 1.0f / 2.0f;
        canvas.scale(sx, sx, qrBitmapWidth / 2, qrBitmapHeight / 2);
        canvas.drawBitmap(logoBitmap, (qrBitmapWidth - logoBitmapWidth) / 2, (qrBitmapHeight - logoBitmapHeight) / 2, null);
        canvas.restore();
        return blankBitmap;
    }


    /**
     * 生成带logo的二维码，logo默认为二维码的1/4
     *
     * @param text 需要生成二维码的文字、网址等
     * @param size 需要生成二维码的大小（）
     * @param mBitmap logo文件
     * @return bitmap
     */
    private Bitmap createQRCodeWithLogo(String text, int size, Bitmap mBitmap) {
        try {
            int IMAGE_HALFWIDTH = size/8;
            Hashtable<EncodeHintType, Object> hints = new Hashtable<>();
            hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
            hints.put(EncodeHintType.MARGIN, 0);//无边框
            /*
             * 设置容错级别，默认为ErrorCorrectionLevel.L
             * 因为中间加入logo所以建议你把容错级别调至H,否则可能会出现识别不了
             */
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            BitMatrix bitMatrix = new QRCodeWriter().encode(text,
                    BarcodeFormat.QR_CODE, size, size, hints);


            int width = bitMatrix.getWidth();//矩阵高度
            int height = bitMatrix.getHeight();//矩阵宽度
            int halfW = width / 2;
            int halfH = height / 2;

            Matrix m = new Matrix();
            float sx = (float) 2 * IMAGE_HALFWIDTH / mBitmap.getWidth();
            float sy = (float) 2 * IMAGE_HALFWIDTH / mBitmap.getHeight();
            m.setScale(sx, sy);
            //设置缩放信息
            //将logo图片按martix设置的信息缩放
            mBitmap = Bitmap.createBitmap(mBitmap, 0, 0,
                    mBitmap.getWidth(), mBitmap.getHeight(), m, false);

            int[] pixels = new int[size * size];
            for (int y = 0; y < size; y++) {
                for (int x = 0; x < size; x++) {
                    if (x > halfW - IMAGE_HALFWIDTH && x < halfW + IMAGE_HALFWIDTH
                            && y > halfH - IMAGE_HALFWIDTH
                            && y < halfH + IMAGE_HALFWIDTH) {
                        //该位置用于存放图片信息
                        //记录图片每个像素信息
                        pixels[y * width + x] = mBitmap.getPixel(x - halfW
                                + IMAGE_HALFWIDTH, y - halfH + IMAGE_HALFWIDTH);
                    } else {
                        if (bitMatrix.get(x, y)) {
                            pixels[y * size + x] = 0xff000000;
                        } else {
                            pixels[y * size + x] = 0xffffffff;
                        }
                    }
                }
            }
            Bitmap bitmap = Bitmap.createBitmap(size, size,
                    Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, size, 0, 0, size, size);
            return bitmap;
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }catch (Exception e){
            return null;
        }
    }


}


