## Fork of SAWS library ##

First, [this library](http://github.com/crawshaw/saws) has saved me all kinds of time, and I really appreciate it.

The only reason for my fork is my usage of this library with [liftweb](http://github.com/dpp/liftweb)

So, the _MediaManager_ class (my addition) makes some assumptions

* You want to use S3 for media storage
* You want to resize the images you upload
* You maybe want to upload video

## Instructions ##


Inside the media manager class, you're going to have to set:

* awsKeyID - your amazon web services key
* awsSecretKey - your aws secret key
* bucket - your bucket name
* rootURL - assuming you're using cloudfront and it's not bucket.s3.amazon.com
* videopath - the place in your bucket you'd like the thing put
* thumbnailpathandprefix - the place in your bucket you want thumbnails, also, a prefix for the filename
* originalpathandprefix - the place in your bucket you want the original image

It should be really obvious how to get more thumbnails out of this, at other sizes, if you dig into the class, but I'll illustrate how it's done here:

## Usage ##

here's an actual example of this used in my code:

	def addMedia( media : List[FileParamHolder] ){
		if(media.size > 0){
			var video_url : String = ""
			var thumbnail_url : String = ""
			var image_url : String = ""
			val message_filename : String = (newMsg.id).toString  //this might be confusing, but newMsg is a model object for messages - .id is it's unique identifier

			for( item <- media ){
				var mediaManager : MediaManager = new MediaManager()

				if(item.name == "video"){
					val filename : String = message_filename+".m4v"
					var url : String = mediaManager.uploadVideo(item.file,item.mimeType, filename )
					newMsg.video_url(url)
					newMsg.save
				}
				if(item.name == "image"){
					val filename : String = message_filename+".png"
					var urls : Array[String] = mediaManager.uploadImage(item.file,item.mimeType, filename )
					newMsg.thumbnail_url(urls(0))
					newMsg.image_url(urls(1))
					newMsg.save
				}
			}
		}
	}


## Specific details about additional image sizes ##

In the _uploadImage_  function, to add a middle sized image and upload it:

	val mediumLocation = cdnBucket("pathTo/Whereyoudlike/thefile/medium-"+fileName)
	forResizing = new ByteArrayInputStream(file)
	var mediumImage : Array[Byte] = ImageResizer.resize(forResizing,320,320)
	mediumLocation.set(mediumImage,contentType)

then, at the end of the function, add the url to `urls`
	
	urls(2) = rootURL + "pathTo/Whereyoudlike/thefile/medium-"+fileName
	
and now it returns a medium URL as well!





	