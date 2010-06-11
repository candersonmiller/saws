import dispatch._
import scala.xml.{NodeSeq, Text}
import _root_.net.liftweb.util._
import net.liftweb.util.JSONParser

import java.net._
import java.io.{ BufferedReader, InputStreamReader, OutputStreamWriter, ByteArrayInputStream, OutputStream, ByteArrayOutputStream }
import java.util.{ Date, Calendar, TimeZone }
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import xml._
import java.net.{ URL, URLEncoder, HttpURLConnection }
import java.text.SimpleDateFormat
import java.util.{ Calendar, TimeZone }
import com.zentus.Base64


import java.awt.Image 
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import java.awt.Graphics2D
import java.awt.AlphaComposite
import java.awt.image.Raster

object ImageResizer {  //obtained/modified from http://stackoverflow.com/questions/1404814/lift-image-upload-resize-store-in-database-display

    def resize(is:java.io.InputStream, maxWidth:Int, maxHeight:Int) : Array[Byte]  = {
        
		val originalImage : BufferedImage = ImageIO.read(is)

        val height = originalImage.getHeight
        val width = originalImage.getWidth
		var returnVal : Array[Byte] = new Array[Byte](10);
		
        if (width == maxWidth && height == maxHeight){
            var dataStream : ByteArrayOutputStream = new ByteArrayOutputStream()
			ImageIO.write(originalImage,"png",dataStream)
			var resizedImage : Array[Byte] = dataStream.toByteArray()
			returnVal = resizedImage
		} else {
            var scaledWidth:Int = width
            var scaledHeight:Int = height
            val ratio:Double = width/height
            if (scaledWidth > maxWidth){
                scaledWidth = maxWidth
                scaledHeight = (scaledWidth.doubleValue/ratio).intValue
            }
            if (scaledHeight > maxHeight){
                scaledHeight = maxHeight
                scaledWidth = (scaledHeight.doubleValue*ratio).intValue
            }
            val scaledBI = new BufferedImage(scaledWidth, scaledHeight,  BufferedImage.TYPE_INT_ARGB)
			val g = scaledBI.createGraphics
			g.setComposite(AlphaComposite.Src)
			g.drawImage(originalImage, 0, 0, scaledWidth, scaledHeight, null);
			g.dispose


			var dataStream : ByteArrayOutputStream = new ByteArrayOutputStream()
			ImageIO.write(scaledBI,"png",dataStream)
			var resizedImage : Array[Byte] = dataStream.toByteArray()
			returnVal = resizedImage

        }
		
		returnVal
		
    }
}

class MediaManager {
	val awsKeyID : String = "your_aws_key"
	val awsSecretKey : String = "your_aws_secret"
	val bucket : String = "your_bucket_name"
	val rootURL : String = "http://cdn.yourthing.com/" //I'm assuming cloudfront usage, but yourthing.s3.amazon.com would work here too
	val videoPath : String = "yourapp/andaplace/youdlike/videos/"
	val thumbnailPathAndPrefix : String = "yourapp/andaplace/youdlike/images/thumbnail-"
	val originalPathAndPrefix : String = "yourapp/andaplace/youdlike/images/original-"
	
	def base64(plain: Array[Byte]) : String =
	        javax.xml.bind.DatatypeConverter.printBase64Binary(plain)
	
	def md5(bytes: Array[Byte]): String = {
		val md = java.security.MessageDigest.getInstance("MD5")	
		md.update(bytes)
	 	new String(Base64.encode(md.digest))
	}

	def uploadVideo( file : Array[Byte], contentType : String, fileName : String ) = {
		val s3 = new com.zentus.s3.S3(awsKeyID,awsSecretKey)
		val cdnBucket = s3(bucket)
		cdnBucket += (videoPath+fileName ,"hai there")
		val myfile = cdnBucket(videoPath+fileName)
		myfile.set(file,contentType)
		rootURL+videoPath+fileName
	}
	
	
	def uploadImage(file : Array[Byte], contentType : String ,fileName : String  ) = {
		val s3 = new com.zentus.s3.S3(awsKeyID,awsSecretKey)
		val cdnBucket = s3(bucket)
		
		cdnBucket += (originalPathAndPrefix+fileName ,"hai there")
		cdnBucket += (thumbnailPathAndPrefix+fileName ,"hai there")
		var forResizing : ByteArrayInputStream = new ByteArrayInputStream(file)
		
		val thumbnail = cdnBucket(thumbnailPathAndPrefix+fileName)
		var thumb : Array[Byte] = ImageResizer.resize(forResizing,58,58)
		thumbnail.set(thumb,contentType)
		

		
		forResizing = new ByteArrayInputStream(file)
		
		val original = cdnBucket(originalPathAndPrefix+fileName)
		var orig : Array[Byte] = file
		original.set(orig,contentType)
		
		
		var urls : Array[String] = new Array[String](2)
		urls(0) = rootURL + thumbnailPathAndPrefix + fileName
		urls(1) = rootURL + originalPathAndPrefix + fileName
		
		urls
	}

}

