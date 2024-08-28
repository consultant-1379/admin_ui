// -----------------------------------------------------------------------------
// * * *  DWcommon - all DWAXE common JavaScript functions  * * *
//
// Copyright Ericsson AB 2011-2013. All rights reserved.
//
// This file is included in and used in ALEX, DHS and X2HTML output formats.
// Function ht() contains free software from sixrevisions.com
//
//
// HISTORY
// -------
// 2011-10-12  xantsib   Review support added, show next/prev changes (R32).
// 2012-02-09  xantsib   Fix for both navigation buttons sometimes inactive.
// 2013-03-14  Saikumar  Functions for tooltip and collapsible (+/-) TOC added.
// 2013-11-27  XALOPEP Added new functions for Doclist filtering.
// 2015-01-07  Suresh    Changes for Document Content Filtering
// 2015-03-12  Suresh    Changes in getCookie to read the cookie value till ;
// 2015-10-13  XBIJPAN   Categorization support for Revamp MOM static Page
// 2015-01-21  xsugant   Support for table sorting XSEIF documents
// 2016-11-11  xshkabd   Support for tc.gif image for yang2html in tooltips
// 2016-02-23  xmamnir   Minor fix for col number calculation in Table sorting JS
// 2017-12-20  xmamnir   YANG Tooltip can be closed by pressing ESC key
// -----------------------------------------------------------------------------

var footer1;
var ttO;
function fadein()
{
  footer1=document.getElementById('footer');
  if(footer1){
  footer1.style.opacity=1;
  footer1.style.filter = 'alpha(opacity=100)';}
}
function fadeout()
{
  footer1=document.getElementById('footer');
  if(footer1){
  footer1.style.opacity=0.1;
  footer1.style.filter = 'alpha(opacity=10)';}
}
function footerfun(){
  var hasVScroll = document.documentElement.scrollHeight > document.documentElement.clientHeight;
  if(!hasVScroll)
  {
    footer1=document.getElementById('footer');
    if(footer1)
      footer1.style.display='none';
  }else{
    setTimeout('fadeout()', 4900);
  }
}

setTimeout('footerfun()', 100);

function getCount(countVal)
{
  c_hits = countVal;
}
var c_count = 0;
var c_curr = "";
var c_id = "c_1";

function showNextChange() {
  if(c_count != c_hits)
  document.getElementById(c_id).style["backgroundColor"] = "";
  if(c_count < (c_hits - 1)) {
    c_id = "c_" + ++c_count;
    document.getElementById(c_id).style["backgroundColor"] = "#ffffbf";
    document.getElementById(c_id).scrollIntoView(true);
    document.getElementById("c_prev_but").style.visibility = "visible";
    document.getElementById("c_prev_but").style.opacity = 1;
    document.getElementById("c_prev_but").style.filter = 'alpha(opacity=100)';
    document.getElementById('c_prev_but').disabled = false;
  }
  else if(c_count == (c_hits - 1)) {
      c_id = "c_" + ++c_count;
    document.getElementById(c_id).style["backgroundColor"] = "#ffffbf";
    document.getElementById(c_id).scrollIntoView(true);
    document.getElementById("c_next_but").style.visibility = "visible";
    document.getElementById("c_next_but").style.opacity = 0.2;
    document.getElementById("c_next_but").style.filter = 'alpha(opacity=20)';
    document.getElementById('c_next_but').disabled = true;
    document.getElementById("c_prev_but").style.visibility = "visible";
    document.getElementById("c_prev_but").style.opacity = 1;
    document.getElementById("c_prev_but").style.filter = 'alpha(opacity=100)';
    document.getElementById('c_prev_but').disabled = false;
  }
  else {
    c_count = c_hits;
    document.getElementById("c_next_but").style.visibility = "visible";
    document.getElementById("c_next_but").style.opacity = 0.2;
    document.getElementById("c_next_but").style.filter = 'alpha(opacity=20)';
    document.getElementById('c_next_but').disabled = true;
    document.getElementById("c_prev_but").style.visibility = "visible";
    document.getElementById("c_prev_but").style.opacity = 1;
    document.getElementById("c_prev_but").style.filter = 'alpha(opacity=100)';
    document.getElementById('c_prev_but').disabled = false;
  }
  if(c_count > c_hits)
    c_curr = c_hits;
  else
    c_curr = c_count + "/" + c_hits;
  document.getElementById("c_prev_but").title = "Show previous change  |  " + c_curr;
  document.getElementById("c_next_but").title = "Show next change  |  " + c_curr;
}

function showPrevChange() {
  document.getElementById(c_id).style["backgroundColor"] = "";
  if(c_count > 2) {
    c_id = "c_" + --c_count;
    document.getElementById(c_id).style["backgroundColor"] = "#ffffbf";
    document.getElementById(c_id).scrollIntoView(true);
    document.getElementById("c_next_but").style.visibility = "visible";
    document.getElementById("c_next_but").style.opacity = 1;
    document.getElementById("c_next_but").style.filter = 'alpha(opacity=100)';
    document.getElementById('c_next_but').disabled = false;
  } else if(c_count == 2) {
    c_id = "c_" + --c_count;
    document.getElementById(c_id).style["backgroundColor"] = "#ffffbf";
    document.getElementById(c_id).scrollIntoView(true);
    document.getElementById("c_prev_but").style.visibility = "visible";
    document.getElementById("c_prev_but").style.opacity = 0.2;
    document.getElementById("c_prev_but").style.filter = 'alpha(opacity=20)';
    document.getElementById('c_prev_but').disabled = true;
    document.getElementById("c_next_but").style.visibility = "visible";
    document.getElementById("c_next_but").style.opacity = 1;
    document.getElementById("c_next_but").style.filter = 'alpha(opacity=100)';
    document.getElementById('c_next_but').disabled = false;
  } else {
    c_count = 0;
    document.getElementById("c_prev_but").style.visibility = "visible";
    document.getElementById("c_prev_but").style.opacity = 0.2;
    document.getElementById("c_prev_but").style.filter = 'alpha(opacity=20)';
    document.getElementById('c_prev_but').disabled = true;
    document.getElementById("c_next_but").style.visibility = "visible";
    document.getElementById("c_next_but").style.opacity = 1;
    document.getElementById("c_next_but").style.filter = 'alpha(opacity=100)';
    document.getElementById('c_next_but').disabled = false;
  }
  if(c_count < 1)
    c_curr = c_hits;
  else
    c_curr = c_count + "/" + c_hits;
  document.getElementById("c_prev_but").title = "Show previous change  |  " + c_curr;
  document.getElementById("c_next_but").title = "Show next change  |  " + c_curr;
}

function hideChange(prlcpy) {
  location.href = prlcpy;
}
function showChange(prlcpy) {
  location.href = prlcpy;
}
var show_toc = true;
var FoundMatch = -1;
var isM = 0;
var ua = navigator.userAgent;
if((ua.indexOf(' Mobile') > 0) || (ua.indexOf(' IEMobile') > 0) || (ua.indexOf(' Opera Mobi') > 0) || (ua.indexOf(' Android') > 0))
{
   isM = 1;
}
function show_hide_TOC() {
var TOC=document.getElementById("TOC");
  if (show_toc) {
    TOC.style.display="none";
    document.getElementById("ce_img1").style.display = "none";
    document.getElementById("ce_img2").style.display = "inline";
    show_toc = false;
  } else {
    TOC.style.display="inline";
    document.getElementById("ce_img1").style.display = "inline";
    document.getElementById("ce_img2").style.display = "none";
    show_toc = true;
  }
}

function show_hide_cont(d_id, i_id1, i_id2, fl) {
var TOC=document.getElementById(d_id);
  if (fl) {

    TOC.style.display="none";
    document.getElementById(i_id1).style.display = "none";
    document.getElementById(i_id2).style.display = "inline";
    fl = false;
  } else {

    TOC.style.display="inline";
    document.getElementById(i_id1).style.display = "inline";
    document.getElementById(i_id2).style.display = "none";
    fl = true;
  }
  return fl;
}

function show_hide_section(a_node) {
    if(a_node != null)
    {
        var img_1 = a_node.firstElementChild;
        var img_2 = img_1.nextSibling;
        if(a_node.parentNode.parentNode.nextSibling.nodeName.toUpperCase() == "DIV")
        {
            var div = a_node.parentNode.parentNode.nextSibling;
        }
        if((img_1 != null) && (img_2 != null)&& (div != null))
        {
            if(img_1.style.display == "none")
            {
                img_1.style.display = "inline";
                img_2.style.display = "none";
                div.style.display = "inline";
                a_node.title = "Click to collapse";
            }
            else if(img_2.style.display == "none")
            {
                img_1.style.display = "none";
                img_2.style.display = "inline";
                div.style.display = "none";
                a_node.title = "Click to expand";
            }
        }
    }
}

var edw = 0;
function edw_path(){
  var dwjs_path = document.getElementsByTagName("script");
  for(i=0;i<dwjs_path.length;i++){
    if(dwjs_path[i].src){
      var index = (dwjs_path[i].src).indexOf("yang.js");
      if(index != -1)
      {
        edw = (dwjs_path[i].src).substring(0,index);
        break;
      }
    }
  }
}

function init(tbl_fol, tbl_doc)
{
  document.getElementById('head2').style.display = "none";
  document.getElementById('main').style.display = "none";
  document.getElementById('hr1').style.display = "none";
  if(isM)
  {
    ChangetoMobileview(tbl_doc, tbl_fol);
    if(!edw)
      edw_path();
    document.getElementById('stylesheet').href=edw+'mob_cvl1.css';
  }
  if(FoundMatch == 0)
  {
     document.write("<div id='nm'>This folder contains no documents matching the current filter selections.</div>");
  }
}

function ChangetoMobileview(tbl_doc, tbl_fol) {
  document.getElementById('head1').style.display = "none";
  document.getElementById('head2').style.display = "block";
  document.getElementById('main').style.display = "block";
  document.getElementById('hr1').style.display = "block";
  var t_id=document.getElementById(tbl_doc);
  if(t_id){
    t_id.cellSpacing="3";
    t_id.cellPadding="5";
    var no_rows=t_id.rows.length;
    var no_clmns;
    var rindex=0;
    while( rindex < no_rows ) {
      no_clmns =t_id.rows[rindex].cells.length;
      while( no_clmns>2 ) {
        no_clmns=no_clmns-1;
        t_id.rows[rindex].deleteCell(no_clmns);
      }
      rindex=rindex+1;
    }
  }
  t_id=document.getElementById(tbl_fol);
  if(t_id){
    t_id.cellSpacing="3";
    t_id.cellPadding="5";
    var no_rows=t_id.rows.length;
    var rw_no=0, rw_id='';
    if(!edw)
       edw_path();
    for(rw_no=1; rw_no<=no_rows; rw_no++){
      rw_id="fi"+rw_no;
      var obj=document.getElementById(rw_id);
      if(obj)
        obj.src=edw+"FC.png";
    }
  }
}
function on_click(info1,info2){
    
    var tagid = info1.id;
	var ele2 = document.getElementById(tagid).children;
	var ele = ele2[0].children;
	var ele3 = ele[0].children;
	var isold = document.getElementsByClassName('descr');
    var flag=1;
    if (isold.length > 0) {
      var desc_ele = ele2[6].children;
	  var Str1 = desc_ele[0].innerHTML + "::" + ele3[0].id ;
      var Str = "";
      if(ele3[0].hasAttribute("rel")){
         var Str_rel1 = ""
		 if (desc_ele[1]){
			Str_rel1 = desc_ele[1].innerHTML;
		 }
		 else if(typeof(tooltip_data) == "undefined"){
            flag=0;
		 }
		else{
			Str_rel1 = tooltip_data[tagid];
		}
         var Str_rel = Str_rel1.concat(ele3[0].rel);
         var destr = "";
         var destr1 = "";
         var array = Str_rel.split('::');
         var arr = array[1].split(':');
         destr1 = " <p class = '" +arr[0]+"'>&nbsp;&nbsp;</p><div id='tclass'>"+arr[1]+"</div>"
         var ar = Str_rel.split('||');
         
         for (var i in ar) {
            if(ar[i] != ""){   
               ar2 = ar[i].substring( ar[i].indexOf(':') + 1 )
               ar1 = ar[i].split(':'); 
               if(ar1[0] == "Module"){  
                    destr = destr + "<p class = 'a4'><span style='color:#C4BD97'>Module:</span><br/>"+"<a href='#"+ar1[1] + "'>" +ar1[1] +"</a></p>";
               }
               else{
                    if(ar1[0] == "Disturbances"){  
                    destr = destr + "<p class = 'a4'><span style='color:red'>"+ar1[0] +":</span><br/>"+ar2 +"</p>";
                    }		
                    else{			  
                    destr = destr + "<p class = 'a4'><span style='color:#C4BD97'>"+ar1[0] +":</span><br/>"+ar2 +"</p>";
	  			  }
               }
            }
         }  
        Str = desc_ele[0].innerHTML + destr + "::" + destr1 + "::::";
      }
      else{
          Str = Str1+ "::::";
      }
    }
	else{
	    var Str1 =  ele2[6].innerHTML + "::" + ele3[0].id ;
        var Str = "";
        if(ele3[0].hasAttribute("rel")){
           var destr = "";
           var destr1 = "";
           var array = ele3[0].rel.split('::');
           var arr = array[1].split(':');
           destr1 = " <p class = '" +arr[0]+"'>&nbsp;&nbsp;</p><div id='tclass'>"+arr[1]+"</div>"
           var ar = ele3[0].rel.split('|');
           
           for (var i in ar) {
              if(ar[i] != ""){   
                 ar2 = ar[i].substring( ar[i].indexOf(':') + 1 )
                 ar1 = ar[i].split(':'); 
                 if(ar1[0] == "Module"){  
                      destr = destr + "<p class = 'a4'><span style='color:#C4BD97'>Module:</span><br/>"+"<a href='#"+ar1[1] + "'>" +ar1[1] +"</a></p>";
                 }
                 else{   
                      destr = destr + "<p class = 'a4'><span style='color:#C4BD97'>"+ar1[0] +":</span><br/>"+ar2 +"</p>";
                 }
              }
           }  
          Str = ele2[6].innerHTML + destr + "::" + destr1 + "::::";
        }
        else{
            Str = Str1+ "::::";
        }
	}
    if(flag==0){
            
            var array = Str.split("::");
            document.addEventListener('keydown', handleKeyDown, false);
            if(isM==1){
            array[0] = "Tooltip data not ready. Please wait...";
               ht.sh(array[0],array[1],array[2],array[3]);}
            else{
               if(array[2]) {location.href=array[2]; } 
            }  
    }
    else{
            var array = Str.split("::");
            document.addEventListener('keydown', handleKeyDown, false);
            if(isM==1){
               ht.sh(array[0],array[1],array[2],array[3]);}
            else{
               if(array[2]) {location.href=array[2]; } 
            }  
    }  
}

function handleKeyDown(e){
  if(e.keyCode === 27) {
   ht.hd();
  }
}

function on_mouseover(info,eve){
  var eeU = eve.clientY;
  var eeL = eve.clientX;
  var Str = String(info);
  var array = Str.split("::");
  if(isM==0){
      ttO = setTimeout(function(){ht.sh(array[0],array[1],array[2],array[3],eeU,eeL);},500); 
    }

}
function on_mouseout(){
  if(ttO){clearTimeout(ttO);}
  if(isM==0){ht.hd(); }
}

/*
 Function ht() contains the javascript from sixrevisions.com tutorial which is a free software.
 This javascript has been customized as per our requirements.
*/
var ht=function(){
var id = 'ht';
var speed = 100;
var timer = 100;
var endalpha = 100;
var alpha = 0;
var Delay = 500;
var count = 0;
var tt,t,c,h;
var div_width;
var maxW=0,minW=0;
var winH,winW;
var minL = 15;
var minT = 15;
var ie;
var browserName;
var iteration = 0;
var isOverlay = 0;

return{
sh:function(ht_v,head,link,w,peU,peL){
  ht_v.replace(/&lt;/g,"<").replace(/&gt;/g,">");
if(count == 0){
  browserName = navigator.appName;
  ie = document.all ? true : false;
}
count = count+1;
iteration = 0;
isOverlay = 0;
if(tt == null){
  tt = document.createElement('div');
  tt.setAttribute('id',id);
  t = document.createElement('div');
  t.setAttribute('id',id + 'top');
  c = document.createElement('div');
  c.setAttribute('id',id + 'cont');
  tt.appendChild(t);
  tt.appendChild(c);
  document.body.appendChild(tt);
  tt.style.opacity = 0;
}
winW = ie ? document.documentElement.offsetWidth : window.innerWidth;
maxW = parseInt(winW*(.9));
if(!edw)
  edw_path();
t.innerHTML = head ?
               (link ?
                 (edw ?
                    "<a href=" + link + " class='hd' style='white-space:nowrap'>" + head + "&nbsp; <img src=\""+edw+"XL.gif\" id=\"link_img\"/></a>"
                    : "<a href=" + link + " class='hd'>" + head + "</a>")
                 : head )
               : "";
if(isM==1){
  //var top = document.getElementById("httop");
  //top.style.minHeight="30px";
  t.innerHTML = edw ? t.innerHTML + "<img id=\"htclose\" onclick=\"ht.hd();\"/>" : t.innerHTML+ "<img id=\"htclose\" onclick=\"ht.hd();\"/>";
}
//ht.pause(650);
if(isM==1) { document.onclick = this.pos; }
else { document.onmousemove = this.pos; }
tt.style.display = 'block';tt.style.top = "-1000px";tt.style.left="-1000px";
c.innerHTML = (isM == 1) ? ht_v : "<span onclick=\"ht.hide();\" style=\"display:block;\">"+ht_v+"</span>";
tt.style.width = w ? w + 'px' : 'auto';
if(!w && ie){
  t.style.display = 'none';
  tt.style.width = tt.offsetWidth;
  t.style.display = 'block';
}
if(head){ minW = (head.length * ((isM==1)?30:15)) + 44; }
if(head &&(tt.offsetWidth < minW)) { tt.style.width = minW; }
if(tt.offsetWidth > maxW){ tt.style.width = maxW + 'px'; }
div_width = tt.offsetWidth;
h = parseInt(tt.offsetHeight) + 1;
if(peU && peL){
  ht.posi(peU,peL);//fix for hover without mouse move
}
clearInterval(tt.timer);
tt.timer = setInterval(function(){ht.fade(1)},timer);
},
pos:function(e){
if(iteration > 0 && isM ==1){ return;}
var u = ie ? event.clientY : e.pageY - window.pageYOffset;
var l = ie ? event.clientX : e.pageX - window.pageXOffset;
var orig_u = u;
var orig_l = l;
winH = ie ? document.documentElement.offsetHeight : window.innerHeight;
u = u-h-10;
if(u < minT)
{
	u = minT;
}
else{

	if ((orig_u >= u) && (orig_u <= u+h))
	{
		u = orig_u + 10;
	}
	
	if(winH < (u+h+5)) { u = (u-h-20);}

}

l = l + (5);
if((l+div_width) > winW )
{
	l = winW - div_width;
	l -= 20;
}

if(l<=minL) { l = minL; }

if( ((orig_u >= u) && (orig_u <= u+h)) && ((orig_l >= l) && (orig_l<=(l+div_width))) && (isM !=1) )
{
  isOverlay = 1; //Case to be handled when cursor goes behind tooltip(for mouse over event)
}

if(document.body.scrollLeft)
  l = l+document.body.scrollLeft;
else if(window.pageXOffset)
  l = l+window.pageXOffset;
else if(document.documentElement.scrollLeft)
  l = l+document.documentElement.scrollLeft;
if(document.body.scrollTop)
  u = u+document.body.scrollTop;
else if(window.pageYOffset)
  u = u+window.pageYOffset;
else if(document.documentElement.scrollTop)
  u = u+document.documentElement.scrollTop;
if(iteration === 0 || iteration % 5 === 0){ 
  if (tt){
tt.style.top = (u)+ 'px';
tt.style.left = (l - 1) + 'px';
  }
}
iteration = iteration+1;
return;
},
posi:function(u,l){
  var orig_u = u; //vertical
  var orig_l = l; //horizontal
  winH = ie ? document.documentElement.offsetHeight : window.innerHeight;
  u = u+15;
  if(winH < (u+h+5)) { u = (u-h-20);}
  if(u<minT) { u=minT;document.onmousemove = null; }
  l = l - (div_width);
  if( ((orig_u >= u) && (orig_u <= u+h)) && ((orig_l >= l) && (orig_l<=(l+div_width))) && (isM !=1) ){
    isOverlay = 1;
  }
  if(l<=minL) { l = minL; }
  if(document.body.scrollLeft)
    l = l+document.body.scrollLeft;
  else if(window.pageXOffset)
    l = l+window.pageXOffset;
  else if(document.documentElement.scrollLeft)
    l = l+document.documentElement.scrollLeft;
  if(document.body.scrollTop)
    u = u+document.body.scrollTop;
  else if(window.pageYOffset)
    u = u+window.pageYOffset;
  else if(document.documentElement.scrollTop)
    u = u+document.documentElement.scrollTop;
  tt.style.top = (u)+ 'px';
  tt.style.left = (l - 1) + 'px';
  return;
},
fade:function(d){
var a = alpha;
if((a != endalpha && d == 1) || (a != 0 && d == -1)){
  var i = speed;
  if(endalpha - a < speed && d == 1){ i = endalpha - a; }
  else if(alpha < speed && d == -1){  i = a; }
  alpha = a + (i * d);
  tt.style.opacity = alpha * .01;
}
else{
  clearInterval(tt.timer);
  if(d == -1){tt.style.display = 'none';tt=null;}
}
},
pause :function(millis){
var date = new Date();
var curDate = null;
do { curDate = new Date(); }
  while(curDate-date < millis);
},
hd:function(){
if( isM !=1 ){
if( (isOverlay != 1) ){
  if(tt && tt.timer){
    clearInterval(tt.timer);
    tt.timer = setInterval(function(){ht.fade(-1);isOverlay = 0;},1);
  }
}
}
else
{
  clearInterval(tt.timer);
  tt.timer = setInterval(function(){ht.fade(-1)},timer);
}
},
hide:function(){
  isOverlay = 0;
  ht.hd();
}
};
}();
/*
XALOPEP Filter logic for doclist for categorization 27 Nov 2013
Added filterDoclist, filterVisibility, and getCookie.
*/
function filterDoclist(tableID,defaultMap,cookieName) {
    var CookieArray;
    var CookieValue = getCookie(cookieName);
    tableID = document.getElementById(tableID);
    CookieValue = CookieValue ? CookieValue : defaultMap;
    if (tableID) {
        var rowIndex = 0, rowCount = tableID.rows.length;
        while( rowIndex < rowCount ) {
            rowClassName = tableID.rows[rowIndex].className.split(' ')[0];
            if (rowClassName.indexOf('-') === 0){
                rowClassName =  rowClassName.replace('-','');
                CookieArray = CookieValue.split('|');
                for(var i = 0; i < CookieArray.length; i = i + 1){
                    CookieArray[i] = parseInt(CookieArray[i], 16);
                }
                if (filterVisibility(rowClassName,CookieArray) === 0){
                    tableID.rows[rowIndex].style.display = 'none';
                }
            }
            rowIndex = rowIndex + 1;
        }
    }    
}

function filterVisibility(givenMap,selectedMap) {
    var eachGiven = givenMap.split('|');
    var mapFlag = 1;
    for (var i = 0; i < eachGiven.length; i = i + 1) {
        if ((selectedMap[i] > 0) && ((selectedMap[i] & parseInt(eachGiven[i], 16)) == 0)) {
            mapFlag = 0;
            break;
        }
    }
    if (mapFlag == 1){
        FoundMatch = 1;
        return 1;
    }else {
       if(FoundMatch!=1)
        FoundMatch = 0;
        return 0;
    }
}

function getCookie(CookieName) {
    var DocCookie = document.cookie, CookieValue = null, StartIndex = DocCookie.indexOf(' ' + CookieName + '=');
    if (StartIndex == -1){
        StartIndex = DocCookie.indexOf(CookieName + '=');
    }
    if (StartIndex == -1){
        DocCookie = null;
    }else{
      StartIndex=DocCookie.indexOf('=', StartIndex) + 1;
      var EndIndex=DocCookie.indexOf(';', StartIndex);
      var indexOfHiphen = DocCookie.indexOf('-', StartIndex);
      if((indexOfHiphen > -1) && (indexOfHiphen < EndIndex))
         EndIndex = indexOfHiphen;
      if (EndIndex == -1){
         EndIndex=DocCookie.length;}
      CookieValue=unescape(DocCookie.substring(StartIndex,EndIndex));
    }
    return CookieValue;
}

function filterTocContent(p_id,visibility){
   var trArray = document.getElementById(p_id).children;
   var index = 0;
   var parent = 0;
   if(trArray.length){
      while(index < trArray.length)
      {
         var trID = trArray[index].id;
         if((trID != null) && ((trID.indexOf("toc_") === 0)))
         {
            trArray[index].style.display = "";
            trID = trID.replace("toc_","");
            var divMatched = document.getElementById("-"+trID);
            //even if the topic display is not none we have check depend on the parent level topics visibility
            if((divMatched != null) && ((divMatched.style.display == "none") || (visibility == 1))){
               trArray[index].style.display = "none";
               parent = 1;
            }
            else parent = 0;            
          }
          var trArray1 = document.getElementById(trArray[index].id).children;
          var index1 = 0;
          if(trArray1.length){
            while(index1 < trArray1.length)
            {
               var divClassVal1 = trArray1[index1].id;
               var divClassVal2 = trArray1[index1].nodeName;
               if(divClassVal2.toLowerCase() === 'ul'){
                   //nested toc handling for DITA documents
                   //sending the parent id and parent topics visibility with current roles
                   filterTocContent(trArray1[index1].id,parent);
               }
               index1++;
            }
          }
         index++;
      }
    }
}
function filterDocContent(defaultMap,cookieName){
    var divArray = document.getElementsByTagName("div");
    var divLen = divArray.length;
    var divIndex = 0;
    var CookieArray;
    var CookieValue = getCookie(cookieName);
    CookieValue = CookieValue ? CookieValue : defaultMap;
    CookieArray = CookieValue.split('|');
    for(var i = 0; i < CookieArray.length; i = i + 1){
        CookieArray[i] = parseInt(CookieArray[i], 16);
    }

    var catMsg = document.getElementById("cat_msg");
    if(catMsg != null)
     catMsg.style.visibility = 'hidden';
    while( divIndex < divLen ) {
        var divClassVal = divArray[divIndex].className;
        var divIdVal = divArray[divIndex].id;
        if((divClassVal != null) && (divClassVal.indexOf('-') === 0) && (divIdVal != null) && (divIdVal.indexOf('-') === 0)){
            divIdVal = divIdVal.replace('-','');
            var msgChapter = document.getElementById("msg_" + divIdVal);
            divClassVal = divClassVal.replace('-','');
            if (filterVisibility(divClassVal,CookieArray) === 0){
                divArray[divIndex].style.display = 'none';
                if(msgChapter != null)
                 msgChapter.style.display = '';
                if(catMsg != null)
                 catMsg.style.visibility = 'visible';
            }
            else
            {
                divArray[divIndex].style.display = '';
                if(msgChapter != null)
                 msgChapter.style.display = 'none';
            }
        }
        divIndex = divIndex + 1;
    }

    var trArray = document.getElementById("toc_1").parentNode.childNodes;
    var index = 0;
    var parent = 0;
    if(trArray.length){
      while(index < trArray.length)
      {
         var trID = trArray[index].id;
         if((trID != null) && ((trID.indexOf("toc_") === 0)))
         {
          trArray[index].style.display = "";
          trID = trID.replace("toc_","");
          var divMatched = document.getElementById("-"+trID);
          if((divMatched != null) && (divMatched.style.display == "none")){
             trArray[index].style.display = "none";
             parent = 1;
          }   
          else if(trID.indexOf(".") > 0){
             var parentCHL = trID.substring(0,trID.lastIndexOf("."));
             var pdivaMatched = document.getElementById("toc_"+parentCHL);
             if((pdivaMatched != null) && (pdivaMatched.style.display == "none"))
              trArray[index].style.display = "none";
          }
          else parent = 0;
          var trArray1 = document.getElementById(trArray[index].id).children;
          var index1 = 0;
          if(trArray1.length){
            while(index1 < trArray1.length)
            {
               var divClassVal1 = trArray1[index1].id;
               var divClassVal2 = trArray1[index1].nodeName;
               if(divClassVal2.toLowerCase() === 'ul'){
                   //nested toc handling for DITA documents
                   //sending the parent id and parent topics visibility with current roles
                   filterTocContent(trArray1[index1].id,parent);
               }
               index1++;
            }
          }
          }
         index++;
      }
    }
}

/* Start : Sorting of pm counters JS */
var pm = 'pm';
var moclass = 'class';
var mim = 'mim';
var sub = 'sub';
var var_up = 'up.gif';
var var_down = 'down.gif';
var var_upandown = 'upandown.gif';
var sortOrder = [];
sortOrder[pm] = 0;
sortOrder[moclass] = 0;
sortOrder[mim] = 0;
sortOrder[sub] = 0;
var pmArray = [], pmRow, pmColumn, cells, cellLength;
var first = 1;
var col = 0;
var sR = 0;
function sortAllpmTable(k) {
var tbody = document.getElementById('allpmtable');
var rows = tbody.rows, rowLengh = rows.length;
if( k == pm ){
    col = 0;
    if(sortOrder[k] != 0){
     sortOrder[k] *= -1;
    }
    else{
     sortOrder[k] = 1;
    }
    sortOrder[moclass] = 0;sortOrder[mim] = 0;sortOrder[sub] = 0;
    iconchange(k,sortOrder[k]);
    sR = sortOrder[k];
}else if ( k == moclass) {
    col = 1;
    if(sortOrder[k] != 0){
     sortOrder[k] *= -1;
    }
    else{
     sortOrder[k] = 1;
    }
    sortOrder[pm] = 0;sortOrder[mim] = 0;sortOrder[sub] = 0;
    iconchange(k,sortOrder[k]);
    sR = sortOrder[k];
}else if ( k == mim) {
    col = 2;
    if(sortOrder[k] != 0){
     sortOrder[k] *= -1;
    }
    else{
     sortOrder[k] = 1;
    }
    sortOrder[pm] = 0;sortOrder[moclass] = 0;sortOrder[sub] = 0;
    iconchange(k,sortOrder[k]);
    sR = sortOrder[k];
}else {
	if(document.getElementById("mimI"))
	{
	  col = 3;
	}
	else{
	  col = 2;
	}

    if(sortOrder[k] != 0){
     sortOrder[k] *= -1;
    }
    else{
     sortOrder[k] = 1;
    }
    sortOrder[pm] = 0;sortOrder[moclass] = 0;sortOrder[mim] = 0;
    iconchange(k,sortOrder[k]);
    sR = sortOrder[k];
}

// Fill the array with values from the table
   for (pmRow = 0; pmRow < rowLengh; pmRow++) {
        cells = rows[pmRow].cells;
        cellLength = cells.length;
        pmArray[pmRow] = [];
        for (pmColumn = 0; pmColumn < cellLength; pmColumn++) {
            pmArray[pmRow][pmColumn] = cells[pmColumn].innerHTML;
        }
   }


// Sort the array by the specified column number (col) and order (sR)
pmArray.sort(function (a, b) {
    var Xa = processTable(a[col]);
    var Xb = processTable(b[col]);

    if(Xa == Xb){
        return 0;
    }else{
        if(Xa > Xb){
            return sR;
        }else{
            return (sR * -1);
        }
    }
});
// Replace existing rows with new rows created from the sorted array
for (pmRow = 0; pmRow < rowLengh; pmRow++) {
    cellLength = pmArray[pmRow].length;
    for (pmColumn = 0; pmColumn < cellLength; pmColumn++) {
        rows[pmRow].cells[pmColumn].innerHTML = pmArray[pmRow][pmColumn];
        if(pmColumn == (pmColumn-1))
        {
           rows[pmRow].cells[pmColumn].setAttribute("class", "lasttd");
        }
    }
}
}

function processTable (input){
if(input.indexOf(">") > -1)
{
  input = input.substr(input.indexOf(">")+1)
}
if(input >= 97){
    input = input.toUpperCase();
    return input;
}else{
    return input;
}
}

function iconchange (x,order){
  if(first == 1){
      if(!edw)
        edw_path();
      var_up = edw + var_up;
      var_down = edw + var_down;
      var_upandown = edw + var_upandown;
	  first = 0;
  }

  if(document.getElementById("pmI")){ document.getElementById("pmI").src = var_upandown;}
  if(document.getElementById("classI")){ document.getElementById("classI").src = var_upandown;}
  if(document.getElementById("mimI")){ document.getElementById("mimI").src = var_upandown;}
  if(document.getElementById("subI")){ document.getElementById("subI").src = var_upandown;}
  //if (toggleState[x] == 0 ) {
  if(order == 1){
    if(document.getElementById(x+'I')){document.getElementById(x+'I').src = var_up;}
  }else if(order == -1){
    if(document.getElementById(x+'I')){document.getElementById(x+'I').src = var_down;}
  }
}
/*End of JS for Sort of All PM Counter*/

// XBIJPAN: Categorization support for Revamp MOM static Page
function filterFrontPage(defaultMap,cookieName) {
    var CookieArray;
    var CookieValue = getCookie(cookieName);
    var divFrontID = document.getElementById("mom_front_page");
    CookieValue = CookieValue ? CookieValue : defaultMap;
    if (divFrontID) {
        var divElementClassName = divFrontID.className;
        CookieArray = CookieValue.split('|');
        for(var i = 0; i < CookieArray.length; i = i + 1){
                CookieArray[i] = parseInt(CookieArray[i], 16);
        }
        if (divElementClassName.indexOf('-') === 0){
            divElementClassName =  divElementClassName.replace('-','');
            if (filterVisibility(divElementClassName,CookieArray) === 0){
                divFrontID.style.display = 'none';
            } 
        }
    }   
}

/*Start: JS for Tables sorting in XSEIF documents*/
var sorted_cols = []; //Stack of sorted column ids
function sort_table(k)
{
    var sOrd = 0, col = 0, j;
    var tblRow;
    var clicked_col = document.getElementById(k);
    var table_id = k.substring(0,k.lastIndexOf("_"));
    var tblArray = [];
    var tbody_orig = document.getElementById(table_id);
    var rows_orig = tbody_orig.rows;
    var tbody_tmp = document.getElementById(table_id).cloneNode(true);
    var tbl_rows = tbody_tmp.rows;
    var tbl_rw_len = tbl_rows.length;
    var sc_len = sorted_cols.length;
    //col = parseInt(k.charAt(k.length-1)) - 1;
    col = parseInt(k.substring(k.lastIndexOf('_')+1)) - 1;
    var ua = window.navigator.userAgent;
    var msie = ua.indexOf("MSIE ");
    if( msie > 0 )
    {
        if(ua[msie+5] != '1') 
        {
            alert("Table sorting feature is disabled in this web browser(IE9). Please upgrade your browser to use this feature");
            return;
        }
    }
    if(clicked_col){
        if(sc_len == 0){ //List is empty create first node
            sorted_cols[sc_len] = [];
            sorted_cols[sc_len][0] = k;
            sorted_cols[sc_len][1] = 1;
            sOrd = 1;
        }
        else{
            for(var j=0;j < sc_len;j++)
            {
                //Toggle the sort order for sorted columns
                if(sorted_cols[j][0] == k)
                {
                    sOrd = sorted_cols[j][1];
                    sOrd = sOrd * (-1);
                    sorted_cols[j][1] = sOrd;
                    break;
                }
                //Column clicked from sorted table, remove old column and add new column id
                if(k.substring(0,k.lastIndexOf("_")) == sorted_cols[j][0].substring(0,sorted_cols[j][0].lastIndexOf("_")))
                {
                    sorted_cols[j][0] = k;
                    sorted_cols[j][1] = 1;
                    sOrd = 1;
                    break;
                }
                //New table entry, add to list
                else if((j+1) == sc_len)
                {
                    sorted_cols[sc_len] = [];
                    sorted_cols[sc_len][0] = k;
                    sorted_cols[sc_len][1] = 1;
                    sOrd = 1;
                    break;
                }
            }
        }
    }
    else{
        return;
    }

    var no_of_cols = clicked_col.parentNode.getElementsByTagName("th").length; // # Of columns in table header

    for (tblRow = 0; tblRow < tbl_rw_len; tblRow++) {
        if(no_of_cols != tbl_rows[tblRow].cells.length) //Check if the row have same #of columns as header
        {
            return;
        }
        tblArray[tblRow] = tbl_rows[tblRow];
    }

    tblArray.sort(function (a, b) 
    {
        var Xa = flCellCont(a.cells[col]);
        var Xb = flCellCont(b.cells[col]);
		//alert("\"" + Xa + "\"" + "\"" + Xb + "\"" );
        if(Xa == Xb)
        {
            return 0;
        }
        else
        {
            if(Xa > Xb)
            {
                return 1 * sOrd;
            }
            else
            {
                return  -1 * sOrd;
            }
        }
    });

    for (tblRow = 0; tblRow < tbl_rw_len; tblRow++) 
    {
        rows_orig[tblRow].innerHTML = tblArray[tblRow].innerHTML;
        rows_orig[tblRow].setAttribute("valign", tblArray[tblRow].getAttribute("valign"));
    }
    changeicon(k,sOrd);
}

function flCellCont (input){
    input = input.textContent;
    input = input.toUpperCase();
    //alert("Input=" + input + ".");
  //  input.replace(/^\s+|\s+$/gm,'');          /^(&nbsp;|<br>)+/, ''
  //  input.replace(/^\n+|\n+$/gm,'');
  //  var re = new RegExp(String.fromCharCode(160), "g");
  //  input.replace(re,' ');
  //  //input.replace(/^\s+\n+|\s+\n+$/g,"");
  //
    input2 = input.replace(/[^a-zA-Z0-9]/g, "");
    if(input2){
        return input2;
    }
    else
        return input;
}

function changeicon(k,ord)
{
    var thead_node = document.getElementById(k).parentNode;
    var th_arr = thead_node.getElementsByTagName("th");
    if(first == 1){
        if(!edw)
            edw_path();
        var_up = edw + var_up;
        var_down = edw + var_down;
        var_upandown = edw + var_upandown;
        first = 0;
    }

    for(var i = 0; i < th_arr.length;i++) 
    {
        var th_id = th_arr[i].id;
        var img_id = "img_".concat(th_id);
        var img_ele = document.getElementById(img_id);
        if(img_ele != null){
            if((k == img_id.substring(4)) && (ord == 1)){
                img_ele.src = var_up;
                img_ele.style.visibility = "visible";
            }
            else if((k == img_id.substring(4)) && (ord == -1)){
                img_ele.src = var_down;
                img_ele.style.visibility = "visible";
            }
            else
                img_ele.style.visibility = "hidden";
        }
    }
}
/*End: JS for Tables sorting in XSEIF documents*/

var imgDiv = null;  // Image being dragged and panned.
var prevX;          // Previous x position of mouse (starting with start pos).
var prevY;          // Previous y position of mouse (starting with start pos).
//var dragIcon = document.createElement('img');


function translateDiv(div, x, y) {
  div.style.left = (div.offsetLeft + x) + 'px';
  div.style.top = (div.offsetTop + y) + 'px';
}
function resizeDiv(div, scaleAmount, maxWidth, minWidth,
                   relResizePosX, relResizePosY) {
  var aspect = div.width / div.height;  // Aspect ratio of this image.
  var oldWidth = div.width;
  var oldHeight = div.height;
  var newWidth = scaleAmount * oldWidth;
  if (newWidth > maxWidth) { newWidth = maxWidth; }
  if (newWidth < minWidth) { newWidth = minWidth; }
  var resizeFract = (newWidth - oldWidth) / oldWidth;
 
  // Scale:
  div.style.width = newWidth + 'px';
  div.style.height = (newWidth / aspect) + 'px';
 
  // Translate:
  div.style.left = ((-oldWidth * resizeFract * relResizePosX) + div.offsetLeft) + 'px';
  div.style.top = ((-oldHeight * resizeFract * relResizePosY) + div.offsetTop) + 'px';
}
function addPanAndZoomToImagesByClass(className) {
  var dragMeElements = document.getElementsByClassName(className);
  for (var i=0; i<dragMeElements.length; i++) {
    imgDiv = dragMeElements[i];
    imgDiv.addEventListener('dragstart', handleDragStart, false);
    imgDiv.addEventListener('dragover', handleDragOver, false);
if (navigator.userAgent.toLowerCase().indexOf('firefox') >= 0) 
    imgDiv.addEventListener("DOMMouseScroll", handleMouseWheel, false);
    else
    imgDiv.addEventListener("mousewheel", handleMouseWheel, false);
  }
}
function handleDragStart(e) {
  //e.dataTransfer.setDragImage(dragIcon, 0, 0);
  imgDiv = e.target;
  prevX = e.clientX;
  prevY = e.clientY;

}
function handleDragOver(e) {
  if (!imgDiv) return;
 
  x = e.clientX;
  y = e.clientY;
  translateDiv(imgDiv, x - prevX, y - prevY);
  prevX = x;
  prevY = y;
}
function handleMouseWheel(e) {
  if (!e.target || e.target.height == 0 || e.target.width == 0) return;
 
  imgDiv = e.target;
  var delta = Math.max(-1, Math.min(1, (e.wheelDelta || -e.detail)));
  var scaleAmount = 1.0 + (delta / 10.0);
  var maxWidth = 5000;
  var minWidth = 50;
  var relResizePosX = e.layerX / e.target.width;
  var relResizePosY = e.layerY / e.target.height;
  resizeDiv(imgDiv, scaleAmount, maxWidth, minWidth,
            relResizePosX, relResizePosY);
}

var copy_visibility = false;           
function showcopyright() {
  if(copy_visibility) {
      document.getElementById("copyright_table").style.display = "none";
      copy_visibility = false;
      document.getElementById("uparrow").style.display = "none";
      document.getElementById("downarrow").style.display = "inline-block";
  }
  else {
      document.getElementById("copyright_table").style.display = "inline-block";
      copy_visibility = true;
      document.getElementById("downarrow").style.display = "none";
      document.getElementById("uparrow").style.display = "inline-block";
  }
}
