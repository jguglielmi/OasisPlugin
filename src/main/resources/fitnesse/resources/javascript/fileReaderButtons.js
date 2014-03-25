
$(function() {
	
	$("#menuFiles").hide().menu();
	$("#fileSelectTable").click(function() {
		return displayFileMenu(this, 'menuFiles');
	});	
	$("[id^=fileReaderBtn]").button(); // find all id's that start with "fileReaderBtn" and set them as buttons.
	$("[id^=fileMnu]").button({icons: {secondary: "ui-icon-triangle-1-s"}});
	$("[id^=fileSelect]").button({text: false, icons: {primary: "ui-icon-triangle-1-s"}});
	$("[id^=fileSelect]").parent().buttonset();
	
	$("#fileImport").click(function() {
		outputText('| import | \n| org.qedsys.file | \n\n');
	});	
	$("#fileTableText").click(function() {
		outputText('| script | text file reader | \n');
	});
	$("#fileTableExcel").click(function() {
		outputText('| script | excel file reader | \n');
	});
	$("#fileSheet").click(function() {
		outputText('| set sheet | sheet name | \n');
	});
	$("#fileCell").click(function() {
		outputText('| get | row num | cell | col num | \n');
	});
	$("#fileRow").click(function() {
		outputText('| get string array from file | filename | with sheet | sheet name | for row | row num | \n');
	});
	$("#fileWholeExcel").click(function() {
		outputText('| get string array from file | filename | with sheet | sheet name | \n');
	});
	$("#fileNextLine").click(function() {
		outputText('| read next line of | text file reader | \n');
	});
	$("#fileEndOfFile").click(function() {
		outputText('| read to end of file | text file reader | \n');
	});
	$("#fileGetTextArray").click(function() {
		outputText('| get array | text file reader | \n');
	});
	$("#fileCloseText").click(function() {
		outputText('| close file | text file reader | \n');
	});
	$('#fileReadBtnFile').click(function () {

		$.get("?fileRead&select", function( data ) {
			if (data) {
				outputText('| set file | ' +  data + ' |\n');
			}
		});
		return false;
	});
});

var fileMenuVar = null;
function displayFileMenu(button, menuName) {
	if (fileMenuVar)//close previous menu if exists
		fileMenuVar.hide();
	var menu = $('#'+ menuName).show().position({
	my: "left top",
	at: "left bottom",
	of: button
	});
	fMenuVar = menu;
	$( document ).one( "click", function() {
		menu.hide();
		fileMenuVar = null;
	});
	return false;
}

jQuery.fn.extend({
	insertAtCaret: function(myValue){
	  return this.each(function(i) {
	    if (document.selection) {
	      //For browsers like Internet Explorer
	      this.focus();
	      var sel = document.selection.createRange();
	      sel.text = myValue;
	      this.focus();
	    }
	    else if (this.selectionStart || this.selectionStart == '0') {
	      //For browsers like Firefox and Webkit based
	      var startPos = this.selectionStart;
	      var endPos = this.selectionEnd;
	      var scrollTop = this.scrollTop;
	      this.value = this.value.substring(0, startPos)+myValue+this.value.substring(endPos,this.value.length);
	      this.focus();
	      this.selectionStart = startPos + myValue.length;
	      this.selectionEnd = startPos + myValue.length;
	      this.scrollTop = scrollTop;
	    } else {
	      this.value += myValue;
	      this.focus();
	    }
	  });
	}
	});

function outputText(text) {
	var tbox = $("#pageContent");
	tbox.insertAtCaret(text);
	//tbox.val(tbox.val() + text);
	//tbox.scrollTop(tbox[0].scrollHeight);
	tbox.focus();
}
