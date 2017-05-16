//[START all]
/*
 * Copyright (c) 2014 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

import com.google.api.client.http.InputStreamContent;
import com.google.api.services.storage.Storage;
import com.google.api.services.storage.model.Bucket;
import com.google.api.services.storage.model.ObjectAccessControl;
import com.google.api.services.storage.model.Objects;
import com.google.api.services.storage.model.StorageObject;
import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.images.Image;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.Transform;
import com.google.appengine.tools.cloudstorage.GcsFileOptions;
import com.google.appengine.tools.cloudstorage.GcsFilename;
import com.google.appengine.tools.cloudstorage.GcsService;
import com.google.appengine.tools.cloudstorage.GcsServiceFactory;
import com.google.appengine.tools.cloudstorage.RetryParams;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletContext;
import org.apache.commons.lang.SerializationUtils;

/**
 * Main class for the Cloud Storage JSON API sample.
 *
 * Demonstrates how to make an authenticated API call using the Google Cloud Storage API client
 * library for java, with Application Default Credentials.
 */
public class StorageSample {

  /** Global instance of the JSON factory. */
  private static final String TEST_FILENAME = "json-test.txt";

  // [START list_bucket]
  /**
   * Fetch a list of the objects within the given bucket.
   *
   * @param bucketName the name of the bucket to list.
   * @return a list of the contents of the specified bucket.
   */
  public static List<StorageObject> listBucket(String bucketName)
      throws IOException, GeneralSecurityException {
    Storage client = StorageFactory.getService();
    Storage.Objects.List listRequest = client.objects().list(bucketName);

    List<StorageObject> results = new ArrayList<StorageObject>();
    Objects objects;

    // Iterate through each page of results, and add them to our results list.
    do {
      objects = listRequest.execute();
      // Add the items in this page of results to the list we'll return.
      results.addAll(objects.getItems());

      // Get the next page, in the next iteration of this loop.
      listRequest.setPageToken(objects.getNextPageToken());
    } while (null != objects.getNextPageToken());

    return results;
  }
  // [END list_bucket]

  // [START get_bucket]
  /**
   * Fetches the metadata for the given bucket.
   *
   * @param bucketName the name of the bucket to get metadata about.
   * @return a Bucket containing the bucket's metadata.
   */
  public static Bucket getBucket(String bucketName) throws IOException, GeneralSecurityException {
    Storage client = StorageFactory.getService();

    Storage.Buckets.Get bucketRequest = client.buckets().get(bucketName);
    // Fetch the full set of the bucket's properties (e.g. include the ACLs in the response)
    bucketRequest.setProjection("full");
    return bucketRequest.execute();
  }
  // [END get_bucket]

  // [START upload_stream]
  /**
   * Uploads data to an object in a bucket.
   *
   * @param name the name of the destination object.
   * @param contentType the MIME type of the data.
   * @param file the file to upload.
   * @param bucketName the name of the bucket to create the object in.
   */
  public static void uploadFile(
      String name, String contentType, File file, String bucketName)
      throws IOException, GeneralSecurityException {
    InputStreamContent contentStream = new InputStreamContent(
        contentType, new FileInputStream(file));
    // Setting the length improves upload performance
    contentStream.setLength(file.length());
    StorageObject objectMetadata = new StorageObject()
        // Set the destination object name
        .setName(name)
        // Set the access control list to publicly read-only
        .setAcl(Arrays.asList(
            new ObjectAccessControl().setEntity("allUsers").setRole("READER")));

    // Do the insert
    Storage client = StorageFactory.getService();
    Storage.Objects.Insert insertRequest = client.objects().insert(
        bucketName, objectMetadata, contentStream);

    insertRequest.execute();
  }
  // [END upload_stream]

  // [START delete_object]
  /**
   * Deletes an object in a bucket.
   *
   * @param path the path to the object to delete.
   * @param bucketName the bucket the object is contained in.
   */
  public static void deleteObject(String path, String bucketName)
      throws IOException, GeneralSecurityException {
    Storage client = StorageFactory.getService();
    client.objects().delete(bucketName, path).execute();
  }
  // [END delete_object]

  /**
   * Exercises the class's functions - gets and lists a bucket, uploads and deletes an object.
   *
   * @param args the command-line arguments. The first argument should be the bucket name.
   */
  public static void main(String[] args) {
    if (args.length < 1) {
      System.out.println("Usage: StorageSample <bucket-name>");
      //System.exit(1);
    }

    //String bucketName = args[0];
    String bucketName = "laykart-165108.appspot.com";
   //String destinationFolder = "laykart-165108.appspot.com/1xConvert";
    
 // [START gcs]
	  
 	  //Allows creating and accessing files in Google Cloud Storage.
 	  final GcsService gcsService = GcsServiceFactory.createGcsService(new RetryParams.Builder()
 	      .initialRetryDelayMillis(10)
 	      .retryMaxAttempts(10)
 	      .totalRetryPeriodMillis(15000)
 	      .build());
 	  // [END gcs]

    try {
      // Get metadata about the specified bucket.
      Bucket bucket = getBucket(bucketName);
      System.out.println("name: " + bucketName);
      System.out.println("location: " + bucket.getLocation());
      System.out.println("timeCreated: " + bucket.getTimeCreated());
      System.out.println("owner: " + bucket.getOwner());
      
      /*File myFile=new File("/laykart-165108.appspot.com/leyKart-images/B1/");
      URL resource = myFile.toURI().toURL();
      //ServletContext context=getServletContext();
		//URL resource=context.getResource("/laykart-165108.appspot.com/leyKart-images/B1/");
		File file = null;
		try {
			file = new File(resource.toURI());
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} */
	    


      // List the contents of the bucket.
      List<StorageObject> bucketContents = listBucket(bucketName);
      if (null == bucketContents) {
        System.out.println(
            "There were no objects in the given bucket; try adding some and re-running.");
      }
      for (StorageObject object : bucketContents) {
	      
	     
    	  //get image Object
    	  System.out.println(object.getName() + " (" + object.getSize() + " bytes)"+"-- Kind"+object.getKind());
    	  System.out.println("iMAGE dATA:::::::"+object.get(object.getName()));
    	  
    	  byte[] imageBytes;
    	  if("leyKart-images/B1/G1.png".equals(object.getName())){
    		  System.out.println("***************************");
    	  ByteArrayOutputStream bos = new ByteArrayOutputStream();
    	  ObjectOutput out = null;
    	  try {
    	    out = new ObjectOutputStream(bos);   
    	    out.writeObject(object);
    	    out.flush();
    	    //imageBytes = bos.toByteArray();
    	    imageBytes = SerializationUtils.serialize(bos.toByteArray());
    	   
    	  } finally {
    	    try {
    	      bos.close();
    	    } catch (IOException ex) {
    	      // ignore close exception
    	    }
    	  }
    	  
    	//[START resize]
  	    // Get an instance of the imagesService we can use to transform images.
  	    ImagesService imagesService = ImagesServiceFactory.getImagesService();

  	    // Make an image directly from a byte array, and transform it.
  	    Image image = ImagesServiceFactory.makeImage(imageBytes);
  	    Transform resize = ImagesServiceFactory.makeResize(125, 75);
  	    Image resizedImage = imagesService.applyTransform(resize, image);
  	    System.out.println("----------------------------");
  	    System.out.println(resizedImage);

  	    // Write the transformed image back to a Cloud Storage object.
  	    gcsService.createOrReplace(
  	        new GcsFilename(bucketName, "resizedImage_125X75" + object.getName()),
  	        new GcsFileOptions.Builder().mimeType("image/jpeg").build(),
  	        ByteBuffer.wrap(resizedImage.getImageData()));
  	    //[END resize]
    	  
    	  } 
    	  
  	  /* if(object.getName()== "leyKart-images/B1/"){
    		  Blob imageData = (Blob)object.get("G1.png");
    		  System.out.println("IIIIIIIIIIIIIIImage Object"+imageData.getBytes().length);
    		  
    		  File myFile=new File("/laykart-165108.appspot.com/leyKart-images/B1/");
    	      URL resource = myFile.toURI().toURL();
    	      //ServletContext context=getServletContext();
    			//URL resource=context.getResource("/laykart-165108.appspot.com/leyKart-images/B1/");
    			File file = null;
    			try {
    				file = new File(resource.toURI());
    			} catch (URISyntaxException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			}
    		  
    		  FileInputStream fileInputStream = new FileInputStream(file);
    		    FileChannel fileChannel = fileInputStream.getChannel();
    		    ByteBuffer byteBuffer = ByteBuffer.allocate((int)fileChannel.size());
    		    fileChannel.read(byteBuffer);

    		    byte[] imageBytes = byteBuffer.array();
    		    System.out.println("Test2");  
    		  
    		  
    	  }*/
    	  
      
    	  
        
        
        
        
       /* //[START resize]
	    // Get an instance of the imagesService we can use to transform images.
	    ImagesService imagesService = ImagesServiceFactory.getImagesService();

	    // Make an image directly from a byte array, and transform it.
	    Image image = ImagesServiceFactory.makeImage(imageBytes);
	    Transform resize = ImagesServiceFactory.makeResize(125, 75);
	    Image resizedImage = imagesService.applyTransform(resize, image);
	    System.out.println("----------------------------");
	    System.out.println(resizedImage);

	    // Write the transformed image back to a Cloud Storage object.
	    gcsService.createOrReplace(
	        new GcsFilename(destinationFolder, "resizedImage_125X75" + object.getName()),
	        new GcsFileOptions.Builder().mimeType("image/jpeg").build(),
	        ByteBuffer.wrap(resizedImage.getImageData()));
	    //[END resize]
*/	    
      
	    
      }
      /*FOR (i=0; i<=bucketContents.size(); i++){
    	  
    	  gcsService.createOrReplace(
      	        new GcsFilename(bucket2, bucketContents[i]),
      	        new GcsFileOptions.Builder().mimeType("image/jpeg").build(),
      	        ByteBuffer.wrap(imageBytes));
    	  
      }*/

      // Create a temp file to upload
      Path tempPath = Files.createTempFile("StorageSample", "txt");
      Files.write(tempPath, "Sample file".getBytes());
      File tempFile = tempPath.toFile();
      tempFile.deleteOnExit();
      // Upload it
      uploadFile(TEST_FILENAME, "text/plain", tempFile, bucketName);

      // Now delete the file
      //deleteObject(TEST_FILENAME, bucketName);

    } catch (IOException e) {
      System.err.println(e.getMessage());
      e.printStackTrace();
      System.out.println("temination here----------->");
      System.exit(1);
    } catch (Throwable t) {
      t.printStackTrace();
      System.exit(1);
    }
  }
}
//[END all]
