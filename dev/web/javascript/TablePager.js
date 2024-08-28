/*
 * ---------------------------------------------------------------------------------------
 *     Copyright (C) 2010 LM Ericsson Limited.  All rights reserved.
 * ---------------------------------------------------------------------------------------
 */

/**
 * Created by IntelliJ IDEA.
 * User: eeipca
 * Date: 01-Aug-2010
 * Time: 16:40:29
 */

function TablePager(table, rowsPerPage, columns) {
  this.tableName = table;
  this.rowsPerPage = rowsPerPage;
  this.columns = columns;
  this.pagesPerTable = 0;
  this.currentPage = 1;
  this.PAGE_NORMAL = "page-normal";
  this.PAGE_SELECTED = "page-selected";
  this.tableHeaders = [];
  this.masterRowData = [];
  this.ddmenuitem = 0;
  this.tbody = null;
  this.thead = null;
  this.isSelectableTable = false;
  var sorterObject = this;
  this.autoPopulateData = null;

  this.setAutoPopulateData = function(data){
    this.autoPopulateData = data;
  };

  this.autoPopulate = function(){
    for (var colIndexPointer in this.autoPopulateData) {
      var colIndex = colIndexPointer.substr(3);
      var value = this.autoPopulateData['ap_'+colIndex];
      for(var row=0;row<this.tbody[0].rows.length;row++){
        var elem = this.tbody[0].rows[row].cells[colIndex].children[0].children[0];
        elem.value = value;
      }
    }
  };

  this.initDefaultTable = function( masterDataList, tableHeaders ){
    this.masterRowData = masterDataList;
    this.tableHeaders = tableHeaders;
    this.isSelectableTable = false;
    this.displayTableWithData(this.masterRowData, this.isSelectableTable);
    this.jumpToPage(1);
  };

   /**
   * Create a page-able table from the input data & header info
   * @param masterDataList The table data
   * @param tableHeaders Column header names
   */
  this.initSelectableTable = function(masterDataList, tableHeaders) {
    this.masterRowData = masterDataList;
    this.tableHeaders = tableHeaders;
    this.isSelectableTable = true;
    this.displayTableWithData(this.masterRowData, this.isSelectableTable);
    this.jumpToPage(1);

    var sortSection = this.thead;
    var sortRow = sortSection[0].rows[0];
    for (var i = 0; i < sortRow.cells.length; i++) {
      sortRow.cells[i].sTable = this;
      sortRow.cells[i].onclick = function () {
        this.sTable.sort(this);
        return false;
      };
    }
  };
  /**
   * Filter the table on a specific field
   * @param column The column the filter is to be applied to
   * @param filterField The field id the filter value is taken from
   */
  this.filterByField = function(column, filterField) {
    var filter = document.getElementById(filterField).value;
    
    if (filter === "") {
      return;
    }
    var visibleRowData = [];
    var visIndex = 0;
    for (var x = 0; x < this.masterRowData.length; x++) {
      var columnText = this.masterRowData[x][column];	
      if (columnText !== null && columnText.match(filter)) {
        visibleRowData[visIndex++] = this.masterRowData[x];
      }
    }
    this.displayTableWithData(visibleRowData, this.isSelectableTable);
  };
  /**
   * Reset all filters & filter input fields
   */
  this.reset = function() {
    var inputs = document.getElementsByTagName("input");
    for (var i = 0; i < inputs.length; i++) {
      if (inputs[i].id.match(RegExp("^filter_"))) {
        inputs[i].value = "";
      }
    }
    this.displayTableWithData(this.masterRowData, this.isSelectableTable);
    this.jumpToPage(1);
  };
  
  /**
   * Function to move to the previous page of the table
   */
  this.prev = function() {
    if (this.currentPage === 1) {
      return;
    }
    this.jumpToPage(this.currentPage - 1);
  };
  /**
   * Function to move to the next page on the table
   */
  this.next = function() {
    if (this.currentPage >= this.pagesPerTable) {
      return;
    }
    this.jumpToPage(this.currentPage + 1);
  };
  /**
   * Function to jump to a specific page number
   * @param jumpToIndex The page number
   */
  this.jumpToPage = function(jumpToIndex) {
    var oldNavPos = document.getElementById('page' + this.currentPage);
    if (oldNavPos !== null) {
      oldNavPos.className = this.PAGE_NORMAL;
      var newNavPos = document.getElementById('page' + jumpToIndex);
      newNavPos.className = this.PAGE_SELECTED;
      this.currentPage = jumpToIndex;
      
      var from = (jumpToIndex - 1) * this.rowsPerPage + 1;
      var to = from + this.rowsPerPage - 1;

      var rows = document.getElementById(this.tableName).rows;
      for (var i = 1; i < rows.length; i++) {
        if (i < from || i > to) {
          rows[i].style.display = 'none';
        }
        else {
          rows[i].style.display = '';
        }
      }
    }
  };
  
  
  
  /**
   * 
   * @param data
   */
  this.displayTableWithData = function(data, includeCheckBoxSelect) {
    var table = document.getElementById(this.tableName);
    if (table !== null) {
      document.getElementById("tabledata").removeChild(table);
    }
    var nav = document.getElementById("bavbar.wrapper");
    if (nav !== null) {
      document.getElementById("navbar").removeChild(nav);
    }
    
    var tableRowCount = data.length - 1;
    this.pagesPerTable = Math.ceil(tableRowCount / this.rowsPerPage);
    this.makeTable(data, includeCheckBoxSelect);
    table = document.getElementById(this.tableName);
    this.tbody = table.getElementsByTagName('tbody');
    this.thead = table.getElementsByTagName('thead');
  };
  
  
  this.createTableNavigator = function(navBarPosition) {
    var wrapper = document.createElement("form");
    wrapper.id = "bavbar.wrapper";
    document.getElementById(navBarPosition).appendChild(wrapper);
    if (this.pagesPerTable > 1) {
      var prev = document.createElement("span");
      prev.className = this.PAGE_NORMAL;
      prev.onclick = Function("tablePager.prev()");
      var pText = document.createTextNode("<< Prev");
      prev.appendChild(pText);
      wrapper.appendChild(prev);
      
      
      wrapper.appendChild(document.createTextNode(" | "));
      for (var page = 1; page <= this.pagesPerTable; page++) {
        var aPage = document.createElement("span");
        aPage.alt = "Page " + page + " of " + this.pagesPerTable;
        aPage.id = "page" + page;
        aPage.className = this.PAGE_NORMAL;
        aPage.onclick = Function("tablePager.jumpToPage(" + page + ")");
        var text = document.createTextNode(page);

        aPage.appendChild(text);
        wrapper.appendChild(aPage);
        wrapper.appendChild(document.createTextNode(" | "));
        if (page % 15 === 0) {
          wrapper.appendChild(document.createElement("br"));
        }
      }
      
      
      var firstPage = document.getElementById("page1");
      firstPage.className = this.PAGE_SELECTED;
      var next = document.createElement("span");
      next.className = this.PAGE_NORMAL;
      next.onclick = Function("tablePager.next()");
      var nText = document.createTextNode("Next >>");
      next.appendChild(nText);
      wrapper.appendChild(next);
      wrapper.appendChild(document.createTextNode(" | "));
      this.appendGoTo(wrapper);
    }
  };
  
  
  this.appendGoTo = function(parentComponent) {
    var ulNav = document.createElement("ul");
    ulNav.id = "nav";
    var liV = document.createElement("li");
    var v = document.createElement("a");
    v.href = "#";
    v.appendChild(document.createTextNode("Go To Page"));
    v.className = this.PAGE_NORMAL;
    liV.appendChild(v);
    ulNav.appendChild(liV);
    var ul = document.createElement("ul");
    liV.appendChild(ul);
    var li = document.createElement("li");
    ul.appendChild(li);
    var pageInput = document.createElement("input");
    pageInput.id = "jumpto_input";
    pageInput.type = "text";
    pageInput.size = 4;
    li.appendChild(pageInput);
    var goBtn = document.createElement("input");
    goBtn.type = "button";
    goBtn.value = "Go";
    goBtn.jumper = this;
    goBtn.onclick = function() {
      var page = document.getElementById("jumpto_input").value;
      this.jumper.jumpToPage(page);
    };
    li.appendChild(goBtn);
    parentComponent.appendChild(ulNav);

  };

  this.makeTableHeader = function (table) {
	    var tableHead = document.createElement('thead');
	    tableHead.id = "sort_header";
	    var tr = document.createElement("tr");
	    tableHead.appendChild(tr);
	    for (var i = 0; i < this.tableHeaders.length; i++) {
	      var colHeader = document.createElement("th");
	      colHeader.id = this.tableHeaders[i];
	      var font = document.createElement("font");

	      font.face = "Verdana,Helvetica,Arial";
	      font.size = 1;
	      var colName = document.createTextNode(this.tableHeaders[i]);
	      colName.id = this.tableHeaders[i];
	      font.appendChild(colName);
	      colHeader.appendChild(font);
	      tr.appendChild(colHeader);
	    }
	    table.appendChild(tableHead);
};

  
  
  
//  this.makeTableBody = function(table, cellList, includeCheckBoxSelection) {
//    var tbody = document.createElement("tbody");
//    for (var i = 0; i < cellList.length; i++) {
//      var row = document.createElement("tr");
//      var rowData = cellList[i];
//
//      if(includeCheckBoxSelection){
//          var reparentCB = document.createElement("td");
//          var cb = document.createElement("input");
//          cb.type = "checkbox";
//          cb.name = "reparentCB";
//          cb.id = rowData[0];
//          cb.checked = cb.defaultChecked = false;
//          reparentCB.appendChild(cb);
//          row.appendChild(reparentCB);
//      }
//
//      
//      for (var j = 0; j < rowData.length; j++) {
//        var tdr = document.createElement("td");
//        var cellFont = document.createElement("font");
//        cellFont.face = "Verdana,Helvetica,Arial";
//        cellFont.size = 1;
//
//        var cellData = rowData[j];
//        var cell = null;
//        if(cellData == '-freetext-'){
//          cell = document.createElement('input');
//          cell.type = "text";
//          cell.size = 11;
//        } else {
//          cell = document.createTextNode(cellData);
//        }
//        cellFont.appendChild(cell);
//        tdr.appendChild(cellFont);
//        cellFont.appendChild(cell);
//        tdr.appendChild(cellFont);
//        row.appendChild(tdr);
//      }
//      tbody.appendChild(row);
//    }
//    table.appendChild(tbody);
//  };
  
  
this.makeTable = function(cellList, includeCheckBoxColumn) {

	    // Create table structure with head and body
		var table = document.createElement("table");
		table.className = "sortable";
		table.id = this.tableName;
		table.border = 1;

		var tableHeader = document.createElement('thead');
		tableHeader.id = "sort_header";

		var tableBody = document.createElement("tbody");

		table.appendChild(tableHeader);
		table.appendChild(tableBody);

		// Build column header
		var tableHeaderRow = document.createElement("tr");
		var headerLen = this.tableHeaders.length;
		for ( var i = 0; i < headerLen; i++) {
			var colHeader = document.createElement("th");
			var tableHeaderText = this.tableHeaders[i];
			
			colHeader.id = tableHeaderText;
			var font = document.createElement("font");
			font.face = "Verdana,Helvetica,Arial";
			font.size = 1;

			
			// Special handling for IE here
			// Either works for Chrome but innerText doesnt work for Firefox
			// in this instance - text doesnt appear in col header.
			// Probably a better way to fix this.
			if (navigator.appName === 'Microsoft Internet Explorer') {
				font.innerText = tableHeaderText;
			} else {
				var colName = document.createTextNode(tableHeaderText);
				colName.id = tableHeaderText;
				font.appendChild(colName);
			}
			
			colHeader.appendChild(font);
			tableHeaderRow.appendChild(colHeader);
		}

		tableHeader.appendChild(tableHeaderRow);

		var ncells = cellList.length;
		for ( var j = 0; j < ncells; j++) {
			if (cellList[j]) {
				var row = document.createElement("tr");
				var rowData = cellList[j];
				if (includeCheckBoxColumn) {
					var reparentCB = document.createElement("td");
					var cb = document.createElement("input");
					cb.type = "checkbox";
					cb.name = "reparentCB";
					cb.id = rowData[0];
					cb.checked = cb.defaultChecked = false;
					reparentCB.appendChild(cb);
					row.appendChild(reparentCB);
				}
				
				var ncols = rowData.length;
				for ( var k = 0; k < ncols; k++) {
					var cellData = rowData[k];
					
					var tdr = document.createElement("td");
					var cellFont = document.createElement("font");
					cellFont.face = "Verdana,Helvetica,Arial";
					cellFont.size = 1;

					var cell = null;
					if (cellData === '-freetext-') {
						cell = document.createElement('input');
						cell.type = "text";
						cell.size = 11;
					} else {
						cell = document.createTextNode(cellData);
					}

					cellFont.appendChild(cell);
					tdr.appendChild(cellFont);
					row.appendChild(tdr);
				}
				tableBody.appendChild(row);
			}
		}

		document.getElementById("tabledata").appendChild(table);
		this.createTableNavigator('navbar');
	};
	
  /**
	 * Sort the table
	 * 
	 * @param cell
	 *            The header to sort by
	 */
  this.sort = function (cell) {
    var column = cell.cellIndex;
    var itm = this.getInnerText(this.tbody[0].rows[1].cells[column]);
    var sortfn = this.sortCaseInsensitive;
    if (itm.match(/true|false/)) {
      sortfn = this.sortCheckboxes;
    } else if (itm.replace(/^\s+|\s+$/g, "").match(/^[\d\.]+$/)) {
      sortfn = this.sortNumeric;
    }
    this.sortColumnIndex = column;
    
    var newRows = new Array();
    var len = this.tbody[0].rows.length;
    for (var j = 0; j < len; j++) {
      newRows[j] = this.tbody[0].rows[j];
    }
    newRows.sort(sortfn);
    if (cell.getAttribute("sortdir") == 'down') {
      newRows.reverse();
      cell.setAttribute('sortdir', 'up');
    } else {
      cell.setAttribute('sortdir', 'down');
    }
    
    for (var i = 0; i < newRows.length; i++) {
      this.tbody[0].appendChild(newRows[i]);
    }

  };
  /**
   * Get the text in a cell
   * @param el
   */
  this.getInnerText = function (el) {
    if (el.children.length > 0 && el.children[0].type == 'checkbox') {
      return "" + el.children[0].checked;
    }
    if (typeof(el.textContent) != 'undefined') {
      return el.textContent;
    }
    if (typeof(el.innerText) != 'undefined') {
      return el.innerText;
    }
    if (typeof(el.innerHTML) == 'string') {
      return el.innerHTML.replace(/<[^<>]+>/g, '');
    }
  };
  /**
   * combobox comparitor
   * @param a Combobox
   * @param b Another combobox
   */
  this.sortCheckboxes = function(a, b) {
    var aa = sorterObject.getInnerText(a.cells[sorterObject.sortColumnIndex]);
    var bb = sorterObject.getInnerText(b.cells[sorterObject.sortColumnIndex]);
    if (aa === bb) {
    	return 0;
    }
    if (aa < bb) {
    	return -1;
    }
    
    return 1;
  };
  /**
   * Compare 2 string
   * @param a
   * @param b
   */
  this.sortCaseInsensitive = function(a, b) {
    var aa = sorterObject.getInnerText(a.cells[sorterObject.sortColumnIndex]).toLowerCase();
    var bb = sorterObject.getInnerText(b.cells[sorterObject.sortColumnIndex]).toLowerCase();
    if (aa === bb) {
    	return 0;
    }
    if (aa < bb) {
    	return -1;
    }
    return 1;
  };
  /**
   * Compare 2 numbers
   * @param a
   * @param b
   */
  this.sortNumeric = function(a, b) {
    var aa = parseFloat(sorterObject.getInnerText(a.cells[sorterObject.sortColumnIndex]));
    if (isNaN(aa)) {
    	aa = 0;
    }
    var bb = parseFloat(sorterObject.getInnerText(b.cells[sorterObject.sortColumnIndex]));
    if (isNaN(bb)) {
    	bb = 0;
    }
    return aa - bb;
  };
  /**
   * 
   */
  this.selectAllCells = function (){
    this.setCellsSelected(true);
  };
  this.deselectAllCells = function(){
    this.setCellsSelected(false);
  };

  /**
   * Selects/deselects checkbox column - ensure that the
   * index in cells[] below is correct for your table
   */
  this.setCellsSelected = function( selected ){
    for(var row=0;row<this.tbody[0].rows.length;row++){
      var cbElement = this.tbody[0].rows[row].cells[0].children[0];
      cbElement.checked = selected;
    }
  };

  this.getTableAsList = function(fieldToSet){
    var tableData = new Array();
    var index = 0;
    for(var row=0;row<this.tbody[0].rows.length;row++){
      var rowData = new Array();
      for(var c = 0 ; c<this.tbody[0].rows[row].cells.length ; c++){
        var parent = this.tbody[0].rows[row].cells[c].children[0];
        var content;
        if(parent.children != null && parent.children.length > 0){
          content = this.tbody[0].rows[row].cells[c].children[0].children[0].value;
        } else {
          content = parent.textContent;
        }
        if(content == ''){
          content = 'null';
        }
        rowData[c] = content;
      }
      tableData[index++] = rowData;
    }
    document.getElementById(fieldToSet).value = tableData.join("|");
  };
  
  
  this.verifyPopulated = function(colsToVerify){
    var colList = colsToVerify.split(",");
    var isOk = true;
    for ( var index = 0; index < colList.length ; index++){
      var colToVerify = colList[index];
      for(var row=0;row<this.tbody[0].rows.length;row++){
        var elem = this.tbody[0].rows[row].cells[colToVerify].children[0].children[0];
        var content = elem.value;
        if(content === null || content === ''){
          elem.style.backgroundColor = 'red';
          isOk = false;
        } else {
          elem.style.backgroundColor = 'white';
        }
      }
    }
    return isOk;
  };
  
  
  this.verifyNoDuplicates = function(colsToVerify) {
    var colList = colsToVerify.split(",");
    var ok = true;
    var tmpCheck = new Object();
    for ( var index = 0; index < colList.length ; index++){
      var colToVerify = colList[index];
      var colIndex = this.tbody[0].rows.length;
      for(var ci = 0; ci<colIndex ; ci++){
        var elem = this.tbody[0].rows[ci].cells[colToVerify].children[0].children[0];
        var text = elem.value;
        if(tmpCheck[text] !== null){
          ok = false;
          elem.style.backgroundColor = 'red';
        } else {
          elem.style.backgroundColor = 'white';
        }
        tmpCheck[text] = true;
      }
    }
    return ok;
  };
  
  /**
   * ACHTUNG!
   * Ensure that whereever you put the checkbox column that
   * it matches with the cells[] index for cbElement below.
   * And that the indexes of the columns of interest are correct
   * @ time of writing 1 to columns.length inclusive (c+1 below). This is really brittle !
   */
  this.getSelectedCells = function(hFieldNName){
    var selectedCells = new Array();
    var index = 0;
    for(var rowIndex=0;rowIndex<this.tbody[0].rows.length;rowIndex++){
      var row = this.tbody[0].rows[rowIndex];
      var selectColumn = row.cells[0];
      var cbElement = selectColumn.children[0];

      if(cbElement.checked){
        var rowData = new Array();
        for(var c = 0 ; c < columns ; c++){
          rowData[c] = this.getInnerText(row.cells[c+1].children[0]);
        }
        selectedCells[index++] = rowData;
      }
    }
    document.getElementById(hFieldNName).value = selectedCells.join("|");
  }
}
