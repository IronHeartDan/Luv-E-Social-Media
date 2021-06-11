const functions = require('firebase-functions');

const admin = require('firebase-admin');
admin.initializeApp();


exports.newReq = functions.database.ref('requests/{To}/{Push}')
.onCreate(async (snapshot,context)=>{
  try {
    var Token,From;
    await snapshot.ref.parent.parent.parent.child("users").child(snapshot.val()).once("value",(snapshot)=>{
      From = snapshot.val().User_name;
    },(errorObject)=>{
      console.log("Failed to read User_name");
    });
  
  
    await snapshot.ref.parent.parent.parent.child("users").child(context.params.To).once("value",(snapshot)=>{
      Token = snapshot.val().Token;
    },(errorObject)=>{
      console.log("Failed to read Token");
    });



    const payload = {
      data : {
      title: "RECIVED A REQUEST",
      body: `${From} Sent You Friend Request`,
      icon: "default",
 }
};
  return admin.messaging().sendToDevice(Token,payload);

  } catch (error) {
    console.log(error)
    return error;
  }
  

});


exports.newMess = functions.database.ref('chats/{Room}/con/{MsgId}/{UserId}/msg')
    .onCreate(async (snapshot, context) => {
      const original = snapshot.val();
      var Token,From,participants,image,user_id,check;

      try {
        
        
        user_id = context.params.UserId;

        await snapshot.ref.parent.parent.parent.parent.child("participants").once("value", (snapshot)=> {
          var ary =  snapshot.val();
          participants = [];
          var i = 0;
          for(x in ary){
            participants[i++] = ary[x];
          }
          
       },  (errorObject)=> {
         console.log("The read failed: " + errorObject.code);
       });


       if(participants[0] === context.params.UserId){
        await snapshot.ref.parent.parent.parent.parent.parent.parent.child("users").child(participants[0]).once("value",(snapshot)=>{
          From = snapshot.val().User_name;
          image = snapshot.val().Profile_pic;
        },(errorObject)=>{
          console.log("Failed to read User_name");
        });

        await snapshot.ref.parent.parent.parent.parent.parent.parent.child("users").child(participants[1]).once("value",(snapshot)=>{
          Token = snapshot.val().Token;
        },(errorObject)=>{
          console.log("Failed to read Token");
        });

        await snapshot.ref.parent.parent.parent.parent.child("in").child(participants[1]).once("value", (snapshot)=> {
        if(snapshot.val() === false || snapshot.val() === null){
          check = 0;
        }else{
          check = 1;
        }
        });

      }else{

        await snapshot.ref.parent.parent.parent.parent.parent.parent.child("users").child(participants[1]).once("value",(snapshot)=>{
          From = snapshot.val().User_name;
          image = snapshot.val().Profile_pic;
        },(errorObject)=>{
          console.log("Failed to read User_name");
        });


        await snapshot.ref.parent.parent.parent.parent.parent.parent.child("users").child(participants[0]).once("value",(snapshot)=>{
          Token = snapshot.val().Token;
        },(errorObject)=>{
          console.log("Failed to read Token");
        });

        await snapshot.ref.parent.parent.parent.parent.child("in").child(participants[0]).once("value", (snapshot)=> {
          if(snapshot.val() === false || snapshot.val() === null){
            check = 0;
          }else{
            check = 1;
          }
          });
      }
    


      
      if(check === 0){
        const payload = {
          data : {
          title: "Message From " + From,
          body: `${original}`,
          icon: "default",
          User_id : user_id,
          User_name : From,
          Profile_pic : image
     }
  };
      return admin.messaging().sendToDevice(Token,payload);
      }

      return null;

      } catch (error) {
        return error;
      }
    });