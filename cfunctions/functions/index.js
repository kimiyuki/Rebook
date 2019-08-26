const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp()
const spawn = require('child-process-promise').spawn;
const path = require('path');
const os = require('os');
const fs = require('fs');
// The Firebase Admin SDK to access the Firebase Realtime Database.
//const admin = require('firebase-admin');
//admin.initializeApp();
// // Create and Deploy Your First Cloud Functions
// // https://firebase.google.com/docs/functions/write-firebase-functions
//
//exports.myFunction = functions.firestore
//  .document('scraps')
//  .onWrite((change, context) => { /* ... */ });
exports.generateThumbnail = functions
    .region('asia-northeast1')
    .storage
    .object()
    .onFinalize(async (object) => {
      const fileBucket = object.bucket; // The Storage bucket that contains the file.
      const filePath = object.name; // File path in the bucket.
      const contentType = object.contentType; // File content type.
      const metageneration = object.metageneration; // Number of times metadata has been generated. New objects have a value of 1.

      if (!object.contentType.startsWith('image/')) {
          return console.log('This is not an image.');
      }
      const fileName = path.basename(filePath);
      // Exit if the image is already a thumbnail.
      if (fileName.startsWith('thumb_')) {
         return console.log('Already a Thumbnail.');
      }

      const bucket = admin.storage().bucket(fileBucket);
      const tempFilePath = path.join(os.tmpdir(), fileName);
      const metadata = { contentType: contentType };

      await bucket.file(filePath).download({destination: tempFilePath});
      console.log('Image downloaded locally to', tempFilePath);
      // Generate a thumbnail using ImageMagick.
      await spawn('convert', [tempFilePath, '-auto-orient', '-thumbnail', '200x200>', tempFilePath]);
      console.log('Thumbnail created at', tempFilePath);
      // We add a 'thumb_' prefix to thumbnails file name. That's where we'll upload the thumbnail.
      const thumbFileName = `thumb_${fileName}`;
      const thumbFilePath = path.join(path.dirname(filePath), thumbFileName);
      // // Uploading the thumbnail.
      await bucket.upload(tempFilePath, {
         destination: thumbFilePath,
           metadata: metadata,
           });
           // Once the thumbnail has been uploaded delete the local file to free up disk space.
          return fs.unlinkSync(tempFilePath);
   });
