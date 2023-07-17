# Where are my Friends?

Aplicación Android que simula el funcionamiento de Google Maps, muestra la ubicación del usuario, de otro usuario que tenga agregado como amigo y permite comunicarse con el mismo a través de funcionalidad de mensajería instantánea

Enlace a la googlesheet de "Where are my friends": https://docs.google.com/spreadsheets/d/14NQcZjZgBxY-fFTvHrJZDMDzcxFb1b5xURdAjeSLgeM/edit#gid=0

Distribución trabajo resumidamente:   

  -Alex: Parte Fronted(Pantallas de la aplicación menos login y register, más interacción con API Google Maps)
  
  -Adrián: Parte backend(Toda la gestión del google spreadsheet + métodos de comunicación HTTP de la carpeta del proyecto Utils) y Parte Fronted (Pantallas Login y Register)

Código de google script:

var FICHERO_USERS_URL = "https://docs.google.com/spreadsheets/d/14NQcZjZgBxY-fFTvHrJZDMDzcxFb1b5xURdAjeSLgeM/edit#gid=0"
//El id se utiliza como una medida de seguridad
var id = "AKfycbwRoz_VM-6332p4laOoGD1WIxmOMnhxRwZI6Lm3ensG5bHkLpV2n8CWAnLo6I6Gklg0jg";
function onOpen() {
  var spreadsheet = SpreadsheetApp.getActive();
  var menuItems = [
    {name: 'Add users test', functionName: 'addUsersTest'},
    {name: 'Update coordinates test', functionName: 'updateCoordinates'},
  ];
  spreadsheet.addMenu('Tests', menuItems);
}
function doGet(e) {
  var id_received = e.parameter.ID;
  if (id_received != id)
    return ContentService.createTextOutput(JSON.stringify(null)).setMimeType(ContentService.MimeType.JSON); 
  var accionParam = e.parameter.ACTION;
  console.log(accionParam);
  var email=e.parameter.EMAIL;
  var params = {}
  if (accionParam == "LOGIN"){
    var password=e.parameter.PASSWORD;
    params["ValidLogin"] = isLogged(email,password);
    params["Token"] = password;
    return ContentService.createTextOutput(JSON.stringify(params)).setMimeType(ContentService.MimeType.JSON); 
  }
  var token=e.parameter.TOKEN;
  if(!isLogged(email,token)){
    params["ValidLogin"] = false;
    return ContentService.createTextOutput(JSON.stringify(params)).setMimeType(ContentService.MimeType.JSON); 
  }
  switch(accionParam.toUpperCase()){
    case "GETFRIENDS":
      var emails = getFriends(email);
      var names = getNames(emails);
      params["FriendsEmail"] = emails;
      params["FriendsName"] = names;
    break;
     case "GETREQUESTFRIENDS":
      var emails = getFriendRequests(email);
      var names = getNames(emails);
      params["FriendRequestsEmail"] = emails;
      params["FriendRequestsName"] = names;
    break;
    case "GETCOORDINATES":
      var emailFriend=e.parameter.EMAILFRIEND;
      params["Coordinates"] = getCoordinates(emailFriend);
      break;
    case "READCHAT":
      var email1 = email;
      var email2 = e.parameter.EMAIL2;
      params["CHAT"] = readChat(email1,email2);
      break;

  }
    return ContentService.createTextOutput(JSON.stringify(params)).setMimeType(ContentService.MimeType.JSON); 
    
}
function doPost(e) {
  var id_received = e.parameter.ID;
  if (id_received != id)
    return ContentService.createTextOutput(JSON.stringify(null)).setMimeType(ContentService.MimeType.JSON); 
  var accionParam = e.parameter.ACTION;
  var email=e.parameter.EMAIL;
  var params = {}
  if (accionParam == "REGISTER"){
    var password=e.parameter.PASSWORD;
    var name=e.parameter.NAME;
    var surname=e.parameter.SURNAME;
    params["ValidRegister"] = addUser(email,name,surname,password);
    params["Token"] = password;
    return ContentService.createTextOutput(JSON.stringify(params)).setMimeType(ContentService.MimeType.JSON); 
  }
  var token=e.parameter.TOKEN;
  if(!isLogged(email,token)){
    params["ValidLogin"] = false;
    return ContentService.createTextOutput(JSON.stringify(params)).setMimeType(ContentService.MimeType.JSON); 
  }
  switch(accionParam.toUpperCase()){
    case "REGISTER":
      var password=e.parameter.PASSWORD;
      var name=e.parameter.NAME;
      var surname=e.parameter.SURNAME;
      params["ValidRegister"] = addUser(email,name,surname,password);
    break;
    case "UPDATECOORDINATES":
      var coordinates=e.parameter.COORDINATES;
      params["ValidUpdate"] = updateCoordinates(email,coordinates);
      break;
    case "REQUESTFRIEND":
      var emailUser = email;
      var friendEmail = e.parameter.FRIENDEMAIL;
      params["ResponseRequest"] = addFriendRequest(emailUser,friendEmail);
      break;  
    case "REQUESTFRIENDRESPONSE":
      var emailUser = email;
      var friendEmail = e.parameter.FRIENDEMAIL;
      var response = e.parameter.RESPONSEREQUEST == "true";
      removeFriendRequest(friendEmail,emailUser);
      if(response)
        params["ResponseRequest"] = addFriend(emailUser,friendEmail);
      else 
        params["ResponseRequest"] = true; //Para indicar si se ha procesado bien la negación de la solicitud
      break;  
      case "REMOVEFRIEND":
      var emailUser = email;
      var friendEmail = e.parameter.FRIENDEMAIL;
      params["ResponseRemove"] = removeFriend(friendEmail,emailUser);
      break;  
      case "WRITECHAT":
        var email1 = email;;
        var email2 = e.parameter.EMAIL2;
        var message = e.parameter.MESSAGE;
        params["WROTE"] = writeChat(email1,email2,message);
        break;
  }
  return ContentService.createTextOutput(JSON.stringify(params) ).setMimeType(ContentService.MimeType.JSON); 

}
function isLogged(email,password){
  var sheet = SpreadsheetApp.openByUrl(FICHERO_USERS_URL).getSheetByName("Users");
  var rowUser = findUser(email);
  if (rowUser  < 0)
    return false;
  var sourceRange = sheet.getDataRange().getValues();
  var userPasswordCol = sourceRange[0].indexOf("Password")+1;
  var cellPassword = sheet.getRange(rowUser, userPasswordCol);
  var passwordDataBase = cellPassword.getValue();
  if (password == passwordDataBase)
    return true;
  return false;
}
function addUser(email, name, surname,password){
  var sheet = SpreadsheetApp.openByUrl(FICHERO_USERS_URL).getSheetByName("Users");
  if (findUser(email) > 0)
    return false;
  var sourceRange = sheet.getDataRange().getValues();
  var userEmailCol = sourceRange[0].indexOf("Email")+1;
  var userNameCol = sourceRange[0].indexOf("Name")+1;
  var userSurnameCol = sourceRange[0].indexOf("Surname")+1;
  var userPasswordCol = sourceRange[0].indexOf("Password")+1;
  var newRow = sheet.getLastRow()+1;
  var cellEmail = sheet.getRange(newRow, userEmailCol);
  cellEmail.setValue(email);
  var cellName = sheet.getRange(newRow, userNameCol);
  cellName.setValue(name);
  var cellSurname = sheet.getRange(newRow, userSurnameCol);
  cellSurname.setValue(surname);
  var cellPassword = sheet.getRange(newRow, userPasswordCol);
  cellPassword.setValue(password);
  return true;
}
function readChat(email,email2){
  if (email < email2)
    var nombre = email+"-"+email2;
  else
    var nombre = email2+"-"+email;
  var sheet = SpreadsheetApp.openByUrl(FICHERO_USERS_URL).getSheetByName(nombre);
  var rows  = sheet.getRange("A1:B").getValues();
  var lastRow = sheet.getLastRow();
  var chat = []
  for (var r=1; r<lastRow; r++) {
    chat.push(rows[r][0])
    chat.push(rows[r][1])
  }
  return chat;
}
function writeChat(email,email2,message){
    if (email < email2)
    var nombre = email+"-"+email2;
  else
    var nombre = email2+"-"+email;
  var sheet = SpreadsheetApp.openByUrl(FICHERO_USERS_URL).getSheetByName(nombre);
  var lastRow = sheet.getLastRow() + 1;
  var cellEmail = sheet.getRange(lastRow,1)
  var cellMessage = sheet.getRange(lastRow,2)
  cellEmail.setValue(email);
  cellMessage.setValue(message);
  return true;
}
function addFriend(email1,email2){
  var sheet = SpreadsheetApp.getActive().getSheetByName('Friends');
  if (findFriendship(email1,email2) > 0)
    return false
  var sourceRange = sheet.getDataRange().getValues();
  var email1Col = sourceRange[0].indexOf("EmailUser1")+1;
  var email2Col = sourceRange[0].indexOf("EmailUser2")+1;
  var newRow = sheet.getLastRow()+1;
  var cellEmail1 = sheet.getRange(newRow,email1Col);
  var cellEmail2 = sheet.getRange(newRow,email2Col);
  cellEmail1.setValue(email1);
  cellEmail2.setValue(email2);
  cellEmail1 = sheet.getRange(newRow+1,email1Col);
  cellEmail2 = sheet.getRange(newRow+1,email2Col);
  cellEmail1.setValue(email2);
  cellEmail2.setValue(email1);
  if (email1 < email2)
    var nombre = email1+"-"+email2;
  else
    var nombre = email2+"-"+email1;
  try{
    var sheet = SpreadsheetApp.getActive().insertSheet(nombre);
    sheet.getRange(1,1).setValue("Sender");
    sheet.getRange(1,2).setValue("Message");
  }catch(e){
    console.log(e);
  }
  return true
}
function addFriendRequest(email1,email2){
  var sheet = SpreadsheetApp.getActive().getSheetByName('FriendRequests');
    if (findFriendship(email1,email2) > 0)
    return 3
  if (findRequestFriendship(email1,email2) > 0)
    return 2
  if (findUser(email2)<0)
    return 1
  var sourceRange = sheet.getDataRange().getValues();
  var email1Col = sourceRange[0].indexOf("EmailUser1")+1;
  var email2Col = sourceRange[0].indexOf("EmailUser2")+1;
  var newRow = sheet.getLastRow()+1;
  var cellEmail1 = sheet.getRange(newRow,email1Col);
  var cellEmail2 = sheet.getRange(newRow,email2Col);
  cellEmail1.setValue(email1);
  cellEmail2.setValue(email2);
  return 0
}
function removeFriendRequest(email1,email2){
  var sheet = SpreadsheetApp.getActive().getSheetByName('FriendRequests');
  var row = findRequestFriendship(email1,email2);
  if (row < 0)
    return false
  var sourceRange = sheet.getDataRange().getValues();
  var email1Col = sourceRange[0].indexOf("EmailUser1")+1;
  var email2Col = sourceRange[0].indexOf("EmailUser2")+1;
  var cellEmail1 = sheet.getRange(row,email1Col);
  var cellEmail2 = sheet.getRange(row,email2Col);
  cellEmail1.setValue("");
  cellEmail2.setValue("");
  return true
}
function removeFriend(email1,email2){
  var sheet = SpreadsheetApp.getActive().getSheetByName('Friends');
  var row = findFriendship(email1,email2);
  if (row < 0)
    return false
  var sourceRange = sheet.getDataRange().getValues();
  var email1Col = sourceRange[0].indexOf("EmailUser1")+1;
  var email2Col = sourceRange[0].indexOf("EmailUser2")+1;
  var cellEmail1 = sheet.getRange(row,email1Col);
  var cellEmail2 = sheet.getRange(row,email2Col);
  cellEmail1.setValue("");
  cellEmail2.setValue("");
  var row = findFriendship(email2,email1);
  cellEmail1 = sheet.getRange(row,email1Col);
  cellEmail2 = sheet.getRange(row,email2Col);
  cellEmail1.setValue("");
  cellEmail2.setValue("");
  if (email1 < email2)
    var nombre = email1+"-"+email2;
  else
    var nombre = email2+"-"+email1;
  try{
      var sheet = SpreadsheetApp.getActive().getSheetByName(nombre);
      SpreadsheetApp.getActive().deleteSheet(sheet);
  }catch(e){
    console.log(e);
  }

  return true
}

function getFriends(email){
  var friendsEmails = [];
  var sheetDatabase = SpreadsheetApp.getActive().getSheetByName('Friends');
  var rows  = sheetDatabase.getRange("A2:B").getValues();
  var lastRow = sheetDatabase.getLastRow() + 1;
  for (var r=0; r<lastRow; r++) {
    if ( rows[r][0] === email) {
      friendsEmails.push(rows[r][1]);
    }
  }
  return friendsEmails;
}
function getNames(emails){
  var friendsNames = [];
  var sheetDatabase = SpreadsheetApp.getActive().getSheetByName('Users');
  var rows  = sheetDatabase.getRange("A2:C").getValues();
  var lastRow = sheetDatabase.getLastRow() + 1;
  for (var r=0; r<emails.length; r++) {
    friendsNames.push(getName(emails[r]));
  }
  return friendsNames;
}
function getName(email){
  var sheetDatabase = SpreadsheetApp.getActive().getSheetByName('Users');
  var rows  = sheetDatabase.getRange("A2:C").getValues();
  var lastRow = sheetDatabase.getLastRow() + 1;
  for (var r=0; r<lastRow; r++) {
    if ( rows[r][0] === email) {
      return rows[r][1] +","+ rows[r][2];
    }
  }
  return -1;
}
function getFriendRequests(email){
  var friends = [];
  var sheetDatabase = SpreadsheetApp.getActive().getSheetByName('FriendRequests');
  var rows  = sheetDatabase.getRange("A2:B").getValues();
  var lastRow = sheetDatabase.getLastRow()
  for (var r=0; r<lastRow; r++) {
    if ( rows[r][1] === email) {
      friends.push(rows[r][0]);
    }
  }
  return friends;
}
function getCoordinates(email){
   var sheet = SpreadsheetApp.openByUrl(FICHERO_USERS_URL).getSheetByName("Users");
  var rowUser = findUser(email);
  if (rowUser  < 0)
    return -1;
  var sourceRange = sheet.getDataRange().getValues();
  var coordinatesCol = sourceRange[0].indexOf("Coordinates")+1;
  var cellCoordinates = sheet.getRange(rowUser,coordinatesCol);
  return cellCoordinates.getValue();
}
function updateCoordinates(email, coordinates){
  var sheet = SpreadsheetApp.openByUrl(FICHERO_USERS_URL).getSheetByName("Users");
  var rowUser = findUser(email)
  if (rowUser  < 0)
    return false;
  var sourceRange = sheet.getDataRange().getValues();
  var userCoordinatesCol = sourceRange[0].indexOf("Coordinates")+1;
  var cellCoordinates = sheet.getRange(rowUser, userCoordinatesCol);
  cellCoordinates.setValue(coordinates);
  return true;
}
function findUser(email) {
  var sheetDatabase = SpreadsheetApp.getActive().getSheetByName('Users');
  var rows  = sheetDatabase.getRange("A2:A").getValues();
  var lastRow = sheetDatabase.getLastRow()
  for (var r=0; r<lastRow; r++) {
    if ( rows[r][0] === email ) {
      return r+2;
    }
  }
  return -1;
}
function findFriendship(email1,email2) {
  var sheetDatabase = SpreadsheetApp.getActive().getSheetByName('Friends');
  var rows  = sheetDatabase.getRange("A2:B").getValues();
  var lastRow = sheetDatabase.getLastRow() + 1;
  for (var r=0; r<lastRow; r++) {
    if ( rows[r][0] === email1 && rows[r][1] === email2 ) {
      return r+2;
    }
  }
  return -1;
}
function findRequestFriendship(email1,email2) {
  var sheetDatabase = SpreadsheetApp.getActive().getSheetByName('FriendRequests');
  var rows  = sheetDatabase.getRange("A2:B").getValues();
  var lastRow = sheetDatabase.getLastRow()
  for (var r=0; r<lastRow; r++) {
    if ( rows[r][0] === email1 && rows[r][1] === email2 ) {
      return r+2;
    }
  }
  return -1;
}
function testLoggIn(){
  var isLog = loggIn("alexMane@gmail.com","mane412")
  console.log(isLog);
  var isLog = loggIn("alexMane@gmail.com","mae412")
  console.log(isLog);
  var isLog = loggIn("imaolo@gmail.com","mane412")
  console.log(isLog);
}
function testCoordinates(){
  var found = updateCoordinates("imanolo@gmail.com",122321)
  console.log(found);
}
function testFindUser(){
  var found = findUser("imaolo@gmail.com")
  console.log(found);
  var found = findUser("alexMane@gmail.com")
  console.log(found);
}
function testFindFriendship(){
  var found = findFriendship("imaolo@gmail.com","alexMane@gmail.com")
  console.log(found);
  var found = findFriendship("alexMane@gmail.com","imaolo@gmail.com")
  console.log(found);
}
function addUsersTest(){
  var isAdd = addUser("alexManeaa@gmail.com","El","Mane","mane412")
  console.log(isAdd);
  isAdd = addUser("imaolaao@gmail.com","Imanolo","Sosas","imanol312")
  console.log(isAdd);
}
function addFriendsTest(){
  var isAdd = addFriend("alexMane@gmail.com","imaolo@gmail.com")
  console.log(isAdd);
}
function addFriendRequestTest(){
  var isAdd = addFriendRequest("adrianBartolome@gmail.com","alexManea@gmail.com")
  console.log(isAdd);
}
function getFriendTest(){
  var friendsEmails = getFriends("alexMane@gmail.com")
  console.log(friendsEmails);
  var friendsNames = getNames(friendsEmails);
  console.log(friendsNames);
}
function getPasswordTest(){
  var password = getPassword("alexMane@gmail.com")
  console.log(password);
}
function removeFriendRequestTest(){
  var isRemove = removeFriendRequest("alexMane@gmail.com","imaolo@gmail.com")
  console.log(isRemove);
}
function removeFriendTest(){
  var email1 = "alexMane@gmail.com";
  var email2 = "imaolo@gmail.com";
  var isRemove = removeFriend(email1,email2)
  console.log(isRemove);
}
function readChatTest(){
  var readed = readChat("alexMane@gmail.com","imaolo@gmail.com")
  console.log(readed);
}
function writeChatTest(){
  var email = "alexMane@gmail.com";
  var email2 = "imaolo@gmail.com";
//  var sheet = SpreadsheetApp.getActive().insertSheet(email+"-"+email2);
//  sheet.getRange(1,1).setValue("Sender");
//  sheet.getRange(1,2).setValue("Message");
  var isWrited = writeChat(email,email2,"Hola que tal")
  console.log(isWrited);
}
function fakeGet() {
  var eventObject = 
    {
      "parameter": {
        "ID" : "AKfycbwRoz_VM-6332p4laOoGD1WIxmOMnhxRwZI6Lm3ensG5bHkLpV2n8CWAnLo6I6Gklg0jg",
        "ACTION": "GETCOORDINATES",
        "EMAIL": "alexMane@gmail.com"
      },
      "contextPath": "",
      "contentLength": -1,
      "queryString": "ACTION=GETCOORDINATES&EMAIL=alexMane@gmail.com&ID=AKfycbwRoz_VM-6332p4laOoGD1WIxmOMnhxRwZI6Lm3ensG5bHkLpV2n8CWAnLo6I6Gklg0jg"
    }
  console.log(doGet(eventObject));
}
function fakePost() {
  var eventObject = 
    {
      "parameter": {
        "ID" : "AKfycbwRoz_VM-6332p4laOoGD1WIxmOMnhxRwZI6Lm3ensG5bHkLpV2n8CWAnLo6I6Gklg0jg",
        "ACTION": "UPDATECOORDINATES",
        "EMAIL": "alexMane@gmail.com"
      },
      "contextPath": "",
      "contentLength": -1,
      "queryString": "ACTION=GETCOORDINATES&EMAIL=alexMane@gmail.com&ID=AKfycbwRoz_VM-6332p4laOoGD1WIxmOMnhxRwZI6Lm3ensG5bHkLpV2n8CWAnLo6I6Gklg0jg"
    }
  console.log(doPost(eventObject));
}
