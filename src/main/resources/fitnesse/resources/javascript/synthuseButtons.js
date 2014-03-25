
$(function() {
	$("[id^=toolBtn]").button(); // find all id's that start with "toolBtn" and set them as buttons.

	$('#toolBtnSynthuse').click(function () {
		$.get("?synth&dialog", function( data ) {
			if (data) {
				//outputText('| do | ' + data + ' |\n');
				alert(data); //exception thrown
			}
			else { //no errors
				setInterval("updateActionsContent();", 2000 ); 
			}
		});
		return false;
	});
});

function updateActionsContent() 
{ 
	$.get("?synth&actions", function( data ) { 
		if (data) {
			outputText(data);
		}
	});
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
	