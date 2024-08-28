 //script to check if the string is a valid email.

function validateFormPassword(parm,maxLen){

	error = "";



	if ( maxLength(parm, maxLen)) {

		error="Password can only be " + maxLen + " characters in length";

	}



	if (!validatePwd(parm)) {

            error="Password should have at least:\n-one upper case letter\n-one lower case letter\n-one number\n-must be a minimum length of 8\n-can not contain any spaces";



    }

 	

	if (error != "") {

            alert (error);

            return false;

    }

   return true;

}



/**Trime white spaces from right and left side of string**/

function trimRightLeftSpace(str) {

	return str.replace(/^\s\s*/, '').replace(/\s\s*$/, '');

}



function phoneCheck(parm){

	var phoneNumPattern=/^\d{2,}$/;

	return phoneNumPattern.test(parm);

}





function emailCheck(str) {

	var emailPattern = /^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,4}$/;

	return emailPattern.test(str);

}



function validateChars(str) {



	var pattern = /^[a-zA-Z0-9]+$/;  

	return pattern.test(str);

}



function validateCharsWithSpace(str) {



	var pattern = /^[a-zA-Z0-9]+[\s{1}a-zA-Z0-9]*$/;  

	return pattern.test(str);





}





function validatePwd(str) {

	 //Original Regex 11.2: 	var pattern = /^.*(?=.{8,255})(?=.*\d)(?=.*[A-Z]).*$/;
	 var pattern = /^(?=.{8,255})(?=.*\d)(?=.*[a-zA-Z])[\S]*$/;
	 

	return pattern.test(str);



}



function submitForm(form,action) {



	 form.action=action;

	 form.method="POST";

	 form.submit();

}





//this function is a comparision function which compares the parameter 

//with different type of values 



function compare(parm,val) {

	  if (parm == "") return true;

	  for (i=0; i<parm.length; i++) {

	    if (val.indexOf(parm.charAt(i),0) == -1) return false;

	  }

	  return true;

	}





//checks if comma is present in the string.

function isComma(parm)

{

	for(i=0;i<parm.length;i++)

	{

		if(parm.charAt(i)==",")

			return true;

				

	}

	return false;

}



//checks if the parameters length is greater than length.



function maxLength(parm,length) 

	{

	var maxLimit=length;

	if(parm.length>maxLimit)

	return true;

	else

	return false;

	}

	

	

	//this function compares the parameter to the val to see if at least one of the 

	//characters of val are present in parm.



	function isValid(parm,val) {

	var c=0;

	  if (parm == "") return true;

	  for (i=0; i<parm.length; i++) {

	    if (val.indexOf(parm.charAt(i),0) != -1)

		c=c+1;

		   

	  }

	  if(c>0)

	  return true;

	  else

	  return false;

	}

	



	

	



	// Declaring required variables

	var digits = "0123456789";

	// non-digit characters which are allowed in phone numbers

	var phoneNumberDelimiters = "()- ";

	// characters which are allowed in international phone numbers

	// (a leading + is OK)

	var validWorldPhoneChars = phoneNumberDelimiters + "+";

	// Minimum no of digits for a phone (internal/external)

	var minDigitsInIPhoneNumber = 2;



	function isInteger(s)

	{   var i;

	    for (i = 0; i < s.length; i++)

	    {   

	        // Check that current character is number.

	        var c = s.charAt(i);

	        if (((c < "0") || (c > "9"))) return false;

	    }

	    // All characters are numbers.

	    return true;

	}

	function trim(s)

	{   var i;

	    var returnString = "";

	    // Search through string's characters one by one.

	    // If character is not a whitespace, append to returnString.

	    for (i = 0; i < s.length; i++)

	    {   

	        // Check that current character isn't whitespace.

	        var c = s.charAt(i);

	        if (c != " ") returnString += c;

	    }

	    return returnString;

	}

	function stripCharsInBag(s, bag)

	{   var i;

	    var returnString = "";

	    // Search through string's characters one by one.

	    // If character is not in bag, append to returnString.

	    for (i = 0; i < s.length; i++)

	    {   

	        // Check that current character isn't whitespace.

	        var c = s.charAt(i);

	        if (bag.indexOf(c) == -1) returnString += c;

	    }

	    return returnString;

	}





	

	

	function checkInternationalPhone(strPhone){

	var bracket=3

	strPhone=trim(strPhone)

	if(strPhone.indexOf("+")>1) return false

	if(strPhone.indexOf("-")!=-1)bracket=bracket+1

	if(strPhone.indexOf("(")!=-1 && strPhone.indexOf("(")>bracket)return false

	var brchr=strPhone.indexOf("(")

	if(strPhone.indexOf("(")!=-1 && strPhone.charAt(brchr+2)!=")")return false

	if(strPhone.indexOf("(")==-1 && strPhone.indexOf(")")!=-1)return false

	s=stripCharsInBag(strPhone,validWorldPhoneChars);

	return (isInteger(s) && s.length >= minDigitsInIPhoneNumber);

	}



// checks optionList for at least one selected item

// if no selection shows error message 'Please select at least one _listName_'

// return: true -> ok, has selection

//		   false-> no selection	

function hasSelection(optionList,listName)

{

    for (var i = 0; i <optionList.length; i++) {

        if (optionList[i].selected) {

            return true;	// has selection -> ok

        }

    }

    

    alert ("Please select at least one "+listName);

    return false; // no selection

	

}





/**

 * @returns Number of selected items in specified Select box

 */

function countSelectedOption(optionList)

{

	var count =0;

    for (var i = 0; i <optionList.length; i++) {

        if (optionList[i].selected) {

        	count++;

        }

    }    

    return count;

}

	

// check value:

//	* length must be less than 30 chars /maxLen/

//	* can't be empty

//	* must match pattern

//

// if value is not valid or empty an alert is shown displaying _valueName_ in the error's argument.

//

//return: true -> ok, valid

//		  false-> no or invalid value

function validateValue(value,valueName,spaceAllowed)

{

	 maxLen = 30;

	error = " ";



//alert ("value: "+value+" valueName: "+valueName);	

    if ( value != "") {

        if ( maxLength (value, maxLen) ) {

            error = valueName+ " can only be " + maxLen + " characters in length ";

        }

        else {

        	if ( spaceAllowed == true && !validateCharsWithSpace (value) )  {

        		error = valueName+ " contains invalid characters ";

        	}

        	if ( spaceAllowed == false && !validateChars (value) ) {

        		error = valueName+ " contains invalid characters ";

        	}

        } // if maxLength else

    }

    else {

    	error = "Please specify "+valueName;

    }

    if (error != " ") {

        alert (error);

        return false;

    }

    

    return true; 

} // func





//

// Validates an optional value. If value is empty returns true, otherwise calls 'validateValue'.

//

function validateOptionalValue(value,valueName,spaceAllowed)

{

//	alert(value );

	if ( value != "" )

		return validateValue(value,valueName,spaceAllowed);



	return true;

} // func



	

///

// Sorts items of the specified '<SELECT>' tag case insensitively

///

function sortList (selElem) {

    var tmpArray = new Array();



    // get all items of list

    for (var i=0;i<selElem.options.length;i++) {

        tmpArray[i] = new Array();

        tmpArray[i][0] = selElem.options[i].text;

        tmpArray[i][1] = selElem.options[i].value;

        tmpArray[i][2] = selElem.options[i].selected;   // store selection state

    } // for



    // sort array

    tmpArray.sort (

        function(x,y){

            var a = String(x).toUpperCase();

            var b = String(y).toUpperCase();

            if (a > b) return 1

            if (a < b) return -1

            return 0;

    });                  // sort



    // clear list values

    while (selElem.options.length > 0) {

        selElem.options[0] = null;

    } // while



    // recreate list, now sorted

    for (var i=0;i<tmpArray.length;i++) {

        var op = new Option(tmpArray[i][0], tmpArray[i][1]);

        selElem.options[i] = op;



        if ( tmpArray[i][2] ) {

            // reselect item if t'was selected before

            selElem.options[i].selected = true;

        }



    } // for



    return;



} // func





// converts value (x) to lowercase   

function changeToLowerCase (x)

{	

	x.value = x.value.toLowerCase();



} // func





