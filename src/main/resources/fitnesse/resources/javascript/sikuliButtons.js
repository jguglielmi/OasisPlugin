
$(function() {
	$("[id^=sikuliBtn]").button(); // find all id's that start with "sikuliBtn" and set them as buttons.
	$("[id^=sikuliMnu]").button({icons: {secondary: "ui-icon-triangle-1-s"}});
	$("[id^=sikuliSelect]").button({text: false, icons: {primary: "ui-icon-triangle-1-s"}});
	$("[id^=sikuliSelect]").parent().buttonset();
	
	$("#menuTables").hide().menu();
	$("#sikuliSelectTable").click(function() {
		return displaySikuliMenu(this, 'menuTables');
	});	
	
	$("#menuClicks").hide().menu();
	$("#sikuliSelectClick").click(function() {
		return displaySikuliMenu(this, 'menuClicks');
	});	
	
	$("#menuKeys").hide().menu();
	$("#sikuliMnuKeys").click(function() {
		return displaySikuliMenu(this, 'menuKeys');
	});
	
	$("#menuAdvanced").hide().menu();
	$("#sikuliMnuAdvanced").click(function() {
		return displaySikuliMenu(this, 'menuAdvanced');
	});
	
	$("#menuCaptureList").hide().menu();
	$("#sikuliSelectCapture").click(function() {
		$.get("?sik&list", function( data ) {
			if (data) {
			    //data = data.replace(/{PREFIX}/g, "<li><a id='sikuliCaptureItem' href='#'>");
			    //data = data.replace(/{POSTFIX}/g, "</a></li>");
				$("#menuCaptureList").html(data);
				$("[id^=sikuliCapturedItem]").click(function() {
					outputText('!-<img src="' + $(this).children('img').eq(0).attr("src") + '" />-!\n');
				}); 
				return displaySikuliMenu($("#sikuliSelectCapture"), 'menuCaptureList');
			}
		});
		return false;
	});	
	$("[id^=sikuliKey]").click(function() {
		outputText('| keyDown | ' + $(this).text() + ' | \n' + '| keyUp | ' + $(this).text() + ' | \n');
	});
	$("#sikuliPresetVars").click(function() {
		outputText('\n!***> Preset variables\n!define TEST_SYSTEM {slim}\n!define BROWSER {firefox}\n!pomFile pom.xml@runtime\n***!\n\n');
	});
	$("#sikuliImport").click(function() {
		outputText('\n| import | \n| com.xebia.sikuli | \n\n');
	});
	$("#sikuliImportSelen").click(function() {
		outputText('\n| import | \n| com.xebia.incubator.xebium | \n\n');
	});	
	$("#sikuliLibrary").click(function() {
		outputText('\n| library |\n| sikuli driver fixture |\n| environment fixture |\n\n');
	});
	$("#sikuliTable").click(function() {
		outputText('| script | sikuli driver fixture | \n');
	});
	$("#sikuliSelenTable").click(function() {
		outputText('| script | selenium driver fixture | \n');
	});	
	$("#sikuliSlidesTable").click(function() {
		outputText('| script | sikuli slides fixture | \n');
	});	
	$("#sikuliComment").click(function() {
		var tbox = $("#pageContent")[0];
		commentSelectedLines(tbox);
	});
	$("#sikuliUncomment").click(function() {
		var tbox = $("#pageContent")[0];
		uncommentSelectedLines(tbox);
	});		
	$("#sikuliDelay").click(function() {
		outputText('| delay | 1000 | \n');
	});	
	$("#sikuliBtnType").click(function() {
		outputText('| type |  |\n');
		return false;
	});	
	$("#sikuliWhileHiddenClick").click(function() {
		outputText('| while | image1 | is hidden then click | image2 |\n');
		//return false;
	});
	$("#sikuliWhileHiddenKeyPress").click(function() {
		outputText('| while | image1 | is hidden then keypress | DOWN |\n');
		//return false;
	});
	$("#sikuliDragDrop").click(function() {
		outputText('| drag | image1 | drop | image2 |\n');
		//return false;
	});	
	$("#sikuliClickOr").click(function() {
		outputText('| click | image1 | or | image2 |\n');
		//return false;
	});	
	$("#sikuliWaitOr").click(function() {
		outputText('| wait | image1 | or | image2 |\n');
		//return false;
	});	
	$("#sikuliEncryptStr").click(function() {
		$.get("?sik&encrypt", function( data ) {
			if (data) {
				outputText(data);
			}
		});
	});	
	$('#sikuliBtnTable').click(function () {
		$.get("?sik&table", function( data ) {
			if (data) {
				outputText('\n' + data + '\n');
			}
		});
		return false;
	});
	$('#sikuliBtnClick').click(function () {
		$.get("?sik&capture", function( data ) {
			if (data) {
				outputText('| click | !-<img src="' + data + '" />-! |\n');
			}
		});
		return false;
	});
	$('#sikuliClick').click(function () {
		$.get("?sik&capture", function( data ) {
			if (data) {
				outputText('| click | !-<img src="' + data + '" />-! |\n');
			}
		});
	});
	$('#sikuliWaitClick').click(function () {
		$.get("?sik&capture", function( data ) {
			if (data) {
				outputText('| wait click | !-<img src="' + data + '" />-! |\n');
			}
		});
	});
	$('#sikuliBtnRightClick').click(function () {
		$.get("?sik&capture", function( data ) {
			if (data) {
				outputText('| rightClick | !-<img src="' + data + '" />-! |\n');
			}
		});
		return false;
	});
	$('#sikuliRightClick').click(function () {
		$.get("?sik&capture", function( data ) {
			if (data) {
				outputText('| rightClick | !-<img src="' + data + '" />-! |\n');
			}
		});
	});
	$('#sikuliBtnDoubleClick').click(function () {
		$.get("?sik&capture", function( data ) {
			if (data) {
				outputText('| doubleClick | !-<img src="' + data + '" />-! |\n');
			}
		});
		return false;
	});
	$('#sikuliDoubleClick').click(function () {
		$.get("?sik&capture", function( data ) {
			if (data) {
				outputText('| doubleClick | !-<img src="' + data + '" />-! |\n');
			}
		});
	});
	$('#sikuliBtnWait').click(function () {
		$.get("?sik&capture", function( data ) {
			if (data) {
				outputText('| wait | !-<img src="' + data + '" />-! |\n');
			}
		});
		return false;
	});
	$('#sikuliBtnHover').click(function () {
		$.get("?sik&capture", function( data ) {
			if (data) {
				outputText('| hover | !-<img src="' + data + '" />-! |\n');
			}
		});
		return false;
	});
	$('#sikuliHover').click(function () {
		$.get("?sik&capture", function( data ) {
			if (data) {
				outputText('| hover | !-<img src="' + data + '" />-! |\n');
			}
		});
	});
	$('#sikuliDisplayText').click(function () {
		outputText('| display text |  | for | 10 |\n');
	});
	$('#sikuliTakeScreenCap').click(function () {
		outputText('| take screen capture | filename_${=!today (yyyyMMddHHmmss)=} |\n');
	});
	$('#sikuliHighlight').click(function () {
		$.get("?sik&capture", function( data ) {
			if (data) {
				outputText('| highlight | !-<img src="' + data + '" />-! |\n');
			}
		});
		//return false;
	});	
	$('#sikuliOffset').click(function () {
		$.get("?sik&toffset", function( data ) {
			if (data) {
				var cords = data.substring(data.indexOf('|') + 1);
				var xc = cords.substring(0, cords.indexOf(','));
				var yc = cords.substring(cords.indexOf(',') + 1);
				outputText('| set target x offset to | ' + xc + ' | set target y offset to | ' + yc + ' |\n');
			}
		});
		//return false;
	});
	$('#sikuliSensitivity').click(function () {
		$.get("?sik&sense", function( data ) {
			if (data) {
				outputText('| set the matching similarity to | ' + data + ' |\n');
			}
		});
		//return false;
	});
	$("#sikuliWaitTime").click(function() {
		outputText('| set wait time to | 8000 |\n');
		//return false;
	});
	$("#sikuliAddNote").click(function() {
		outputText('| note |  |\n');
		//return false;
	});
	$('#sikuliBtnCapture').click(function () {
		$.get("?sik&capture", function( data ) {
			if (data) {
				outputText('!-<img src="' + data + '" />-!');
			}
		});
		return false;
	});
});

$(getCurrentSikuliScriptPath);
$(checkSikuliShowToolbar);


var sikuliMenuVar = null;
function displaySikuliMenu(button, menuName) {
	if (sikuliMenuVar)//close previous menu if exists
		sikuliMenuVar.hide();
	var menu = $('#'+ menuName).show().position({
	my: "left top",
	at: "left bottom",
	of: button
	});
	sikuliMenuVar = menu;
	$( document ).one( "click", function() {
		menu.hide();
		sikuliMenuVar = null;
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

function getInputSelectionPostions(el) {
    var start = 0, end = 0, normalizedValue, range, textInputRange, len, endRange;

    if (typeof el.selectionStart == "number" && typeof el.selectionEnd == "number") {
        start = el.selectionStart;
        end = el.selectionEnd;
    } else {
        range = document.selection.createRange();

        if (range && range.parentElement() == el) {
            len = el.value.length;
            normalizedValue = el.value.replace(/\r\n/g, "\n");

            // Create a working TextRange that lives only in the input
            textInputRange = el.createTextRange();
            textInputRange.moveToBookmark(range.getBookmark());

            // Check if the start and end of the selection are at the very end
            // of the input, since moveStart/moveEnd doesn't return what we want
            // in those cases
            endRange = el.createTextRange();
            endRange.collapse(false);

            if (textInputRange.compareEndPoints("StartToEnd", endRange) > -1) {
                start = end = len;
            } else {
                start = -textInputRange.moveStart("character", -len);
                start += normalizedValue.slice(0, start).split("\n").length - 1;

                if (textInputRange.compareEndPoints("EndToEnd", endRange) > -1) {
                    end = len;
                } else {
                    end = -textInputRange.moveEnd("character", -len);
                    end += normalizedValue.slice(0, end).split("\n").length - 1;
                }
            }
        }
    }
    return {
        start: start,
        end: end
    };
}


function getLinePostionsOfSelection(el) {
    var start = 0, end = 0;
    var selectionPostion = getInputSelectionPostions(el);
    var normalizedValue = el.value.replace(/\r\n/g, "\n");
    for (var i = 0 ; i < normalizedValue.length -1; i++) {
        if (normalizedValue.charAt(i) == "\n" && i < selectionPostion.start) { //find start line
            start = i;
        }
        else if(normalizedValue.charAt(i) == "\n" && i >= selectionPostion.end -1 ) {
            end = i;
            break;
        }
    }
    if (end == 0) {
        end = normalizedValue.length;
    }
    return {
        start: start,
        end: end
    };
}

function commentSelectedLines(txtObject)
{
    //var selectedText = getInputSelectionPostions(txtObject);
    var selectedTextPos = getLinePostionsOfSelection(txtObject);
    var selectedLines = txtObject.value.substring(selectedTextPos.start, selectedTextPos.end+1);
    var selectedText = selectedLines.replace(/(.+?)\n/g, "| note | $1 |\n");
    txtObject.value = txtObject.value.substring(0,selectedTextPos.start) + selectedText + txtObject.value.substring(selectedTextPos.end+1, txtObject.value.length);
    //alert(selectedText);
}

function uncommentSelectedLines(txtObject)
{
    var selectedTextPos = getLinePostionsOfSelection(txtObject);
    var selectedLines = txtObject.value.substring(selectedTextPos.start, selectedTextPos.end+1);
    //alert(selectedLines);
    var selectedText = selectedLines.replace(/\| note \| ([^\n]+?) \|\n/g, "$1\n");
    txtObject.value = txtObject.value.substring(0,selectedTextPos.start) + selectedText + txtObject.value.substring(selectedTextPos.end+1, txtObject.value.length);    
    
}

function outputText(text) {
	var tbox = $("#pageContent");
	tbox.insertAtCaret(text);
	//tbox.val(tbox.val() + text);
	//tbox.scrollTop(tbox[0].scrollHeight);
	tbox.focus();
}

function getCurrentSikuliScriptPath() {
	$.get("?sik&path", function( data ) {
		$("#sikuliPath").html('' + data);
	});
}

function checkSikuliShowToolbar() {
	$.get("?sik&tools", function( data ) {
		if (data == "false") {
			$("#sikuliButtons").hide();
		}
		else {
			$("#sikuliButtons").show();
		}
	});
}
