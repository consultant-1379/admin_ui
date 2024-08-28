//----------------------
//Ericsson AB 2012
//Date: 2012-06-18
//Desc: Script to find and alert user for any XSS vulnerability if any in the form text/textarea fields
//----------------------

/**
* Function to filter the given string based upon some
* predefined filters which may cause XSS attack.
*/
function filterStringForXSS(impureStr){
//Array Object for filters
var xssFilters=new Array();

//Object to store filtered String
var pureStr = impureStr ;

//List resource: http://ha.ckers.org/xss.html
xssFilters[0] = /(<script\b[^>]*>(.*?)<\/script\b[^>]*>)/gi;
xssFilters[1] = /(<embed\b[^>]*>(.*?)<\/embed\b[^>]*>)/gi;
xssFilters[2] = /(<script\b[^>]*>)/gi;
xssFilters[3] = /(<embed\b[^>]*>)/gi;
xssFilters[4] = /(<object\b[^>]*>(.*?)<\/object\b[^>]*>)/gi;
xssFilters[5] = /(<object\b[^>]*>)/gi;
xssFilters[6] = /(<img\b[^>]*>)/gi;
xssFilters[7] = /(<body\b[^>]*>)/gi;
xssFilters[8] = /(<input\b[^>]*>)/gi;
xssFilters[9] = /(<iframe\b[^>]*>(.*?)<\/iframe\b[^>]*>)/gi;
xssFilters[10] = /(<iframe\b[^>]*>)/gi;
xssFilters[11] = /(<BGSOUND\b[^>]*>)/gi;
xssFilters[12] = /(<br\b[^>]*>)/gi;
xssFilters[13] = /(<link\b[^>]*>)/gi;
xssFilters[14] = /(<table\b[^>]*>)/gi;
xssFilters[15] = /(javascript(\s)*:(.*?))/gi;
xssFilters[16] = /(<a\b[^>]*>)/gi;
xssFilters[17] = /(<style\b[^>]*>)/gi;
xssFilters[18] = /(<meta\b[^>]*>)/gi;
xssFilters[19] = /(<layer\b[^>]*>)/gi;
xssFilters[20] = /(<title\b[^>]*>)/gi;
xssFilters[21] = /(<div\b[^>]*>)/gi;
xssFilters[22] = /(<applet\b[^>]*>)/gi;
xssFilters[23] = /(<html\b[^>]*>)/gi;
xssFilters[24] = /(<frameset\b[^>]*>)/gi;
xssFilters[25] = /(<xml\b[^>]*>)/gi;
xssFilters[26] = /(<comment\b[^>]*>)/gi;

//Encoded filters for different filters
//For script tag
xssFilters[27] = /((&#60)(&#115)(&#99)(&#114)(&#105)(&#112)(&#116))/gi; //dec for script without semicolon
xssFilters[28] = /((&#x3C;)(&#x73;)(&#x63;)(&#x72;)(&#x69;)(&#x70;)(&#x74;))/gi; // hex for script  with semicolon
xssFilters[29] = /((&#x3C)(&#x73)(&#x63)(&#x72)(&#x69)(&#x70)(&#x74))/gi; // hex for script  without semicolon
//For embed tag
//&#x3C;&#x65;&#x6D;&#x62;&#x65;&#x64;
//&#60&#101&#109&#98&#101&#100
xssFilters[30] = /((&#60)(&#101)(&#109)(&#98)(&#101)(&#100))/gi; //dec for embed tag without semicolon
xssFilters[31] = /((&#x3C;)(&#x65;)(&#x6D;)(&#x62;)(&#x65;)(&#x64;))/gi; // hex for embed  tag with semicolon
xssFilters[32] = /((&#x3C)(&#x65)(&#x6D)(&#x62)(&#x65)(&#x64))/gi; // hex for embed  tag without semicolon

xssFilters[33] = /('onMouseOver='alert(1)\b[^>]*>)/gi;
xssFilters[34] = /(<span\b[^>]*>)/gi;
xssFilters[35] = /(<a\b[^>]*>)/gi;
xssFilters[36] = /(<input\b[^>]*>)/gi;


//Loop to traverse every filter to make sure field content is free from all of them
for (var i = 0; i < xssFilters.length; i++) {
//Find whether the current filter string  is present in field or not
var  test1 =  pureStr.match(xssFilters[i]);
if(test1 != null){
  //Present. XSS vulnerability found. So, replace the filter with empty string
  pureStr = pureStr.replace(xssFilters[i],"");
}else{
  //Present. Safe to use
  pureStr = pureStr ;
}
}
//Return the safe string
return pureStr;
}

/**
* Function to iterate through all the form
* fields of type INPUT text/textarea of current webpage and 
* call filterStringForXSS for each of them to find any XSS vulnerability in them and warn user.
*/
function filterFields(){

var boxes_input = document.getElementsByTagName("INPUT");
var boxes_textarea = document.getElementsByTagName("TEXTAREA");
var isXSS_Safe_input = true ;
var isXSS_Safe_textarea = true;
var isXSS_Safe_select = true;
var sels = document.getElementsByTagName("select");

//Iterate through all the form fields of type INPUT text/textarea
for(var i = 0; i < boxes_input.length ; i++){
if(boxes_input[i].type == "text"){
  var boxID = boxes_input[i] ;
  var boxValue = boxes_input[i].value ;
  var filteredValue = filterStringForXSS(boxValue) ;
    if(filteredValue != boxValue){
      //Set the flag to false
    	isXSS_Safe_input = false ;
      //Change the background and text color of this field
      boxID.style.background = 'red' ;

      boxID.style.color = 'white' ;
      
      //Open this below line if you want to replace the value with filtered value in form field
      //boxID.value = filteredValue ;
    }else{
      boxID.style.background = 'white' ;
      boxID.style.color = 'black' ;
    }
}
}
for(var i = 0; i < boxes_textarea.length ; i++){
	      var boxID = boxes_textarea[i] ;
	      var boxValue = boxes_textarea[i].value ;
	      var filteredValue = filterStringForXSS(boxValue) ;
	        if(filteredValue != boxValue){
	          //Set the flag to false
	          isXSS_Safe_textarea = false ;
	          //Change the background and text color of this field
	          boxID.style.background = 'red' ;
	          boxID.style.color = 'white'; 
	          //Open this below line if you want to replace the value with filtered value in form field
	          //boxID.value = filteredValue ;
	        }else{
	          boxID.style.background = 'white' ;
	          boxID.style.color = 'black' ;
	        }
}

for(var i=0; i<sels.length;i++){
  var sel = sels[i];
   var boxValue = sel.options[sel.selectedIndex].text;
   var filteredValue = filterStringForXSS(boxValue) ;
   if(filteredValue != boxValue){
     //Set the flag to false
     isXSS_Safe_select = false ;
     //Change the background and text color of this field
     sel.style.background = 'red' ;
     sel.style.color = 'white'; 
     //Open this below line if you want to replace the value with filtered value in form field
     //boxID.value = filteredValue ;
   }else{
     sel.style.background = 'white' ;
     sel.style.color = 'black' ;
   }
}

	  
//Display alert box to user, if any of the field is unsafe for XSS attack.
if((!isXSS_Safe_input)  || (!isXSS_Safe_textarea) || (!isXSS_Safe_select) ){
//alert("XSS Vulnerability found....");
for(var i = 0; i < boxes_input.length; i++){
    if(boxes_input[i].type == "text" ){
            var boxID = boxes_input[i] ;
            boxID.style.background = 'white' ;
            boxID.style.color = 'black' ;
    }
}//for
for(var i = 0; i < boxes_textarea.length; i++){
            var boxID = boxes_textarea[i] ;
            boxID.style.background = 'white' ;
            boxID.style.color = 'black' ;
//for
}

for(var i=0; i<sels.length;i++){
    var boxID = sels[i][i] ;
            boxID.style.background = 'white' ;
            boxID.style.color = 'black' ;
}
}

//Returns true if no XSS vulnerability has been found otherwise false.
return (isXSS_Safe_input && isXSS_Safe_textarea && isXSS_Safe_select);
}

function replaceBrowserState() {
window.history.replaceState(null, null, window.location.pathname);
}

