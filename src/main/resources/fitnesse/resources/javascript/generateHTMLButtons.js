
$(function() {
	$('#generateHTMLButton').click(function () {

		$.get("?reportGenerate&select", function( data ) {
		});
		return false;
	});
	
	$('#generateLatestHTMLButton').click(function () {

		$.get("?reportGenerate&latest", function( data ) {
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
