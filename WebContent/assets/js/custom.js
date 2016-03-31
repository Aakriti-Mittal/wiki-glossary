$(document).ready(function(){
	
//for highlighting(bolding) the search_string
	$.fn.wrapInTag = function(opts) {
		  
		  var tag = opts.tag || 'strong',
		      words = opts.words || [],
		      regex = RegExp(words.join('|'), 'gi'),
//		      replacement = '<'+ tag +'>$&</'+ tag +'>';
		      replacement = "<span style='background-color: #FFFF00'"+ "" +">$&</"+ "span" +">";
//		  <span style="background-color: #FFFF00">CCU</span>
		  return this.html(function() {
		    return $(this).text().replace(regex, replacement);
		  });
		};


//for hiding the menu options
	var user_privilege=$(".user_privilege").text().trim();
	if(user_privilege==2)
		{
		$("#delete_button,#add_new_button").css("display","block");
		}
	else {
		$("#delete_button,#add_new_button").remove();
	}
	
/* for the autocomplete data */	
var availableTags = [""];

$.ajax({
    url: 'Conn_add_new',
    data: {
        postVariableName: "gettingTheTitleAutocomplete",
    },
    success:function(data){
    	$("#contents-for-autocomplete").html(data);
    	$('.doc-title-value').each( function(){
    		availableTags.push($(this).text());
    	});
    	$('.doc-title-value').remove();
    },
    type: 'POST'
});
$(function() {
    $( "#search-text-box,.add-new-div .input_title,.delete-div .input_title" ).autocomplete({
      source: availableTags
    });
  });
/* end for the autocomplete data */	

/* main search function*/
function postSearchFunction(){
	setTimeout(function () { $("ul#ui-id-1, ul#ui-id-2, ul#ui-id-3").hide(); }, 1000);
	$("#no-result-found").hide();
	var search_text = $("#search-text-box").val();
	search_text=search_text.trim().toLowerCase();
	$("#search-title-span").text(search_text);
  if(search_text == "")
  {
    $(".search-title").hide();
    $("#topic-list").html("");
  }
else
  {
    $(".search-title").show();
	
	$.ajax({
	    url: 'Conn_add_new',
	    data: {
	        postVariableName: "gettingTheDoc",
	        search_text: search_text
	    },
	    success:function(data){
	    	$("#topic-list").html(data);
	    	if($(".topic").length==0)
	    	{
	    		$("#no-result-found").show();
	    	}
	    	else{
	    		executePagination();
	    	}
			$('.more-description').wrapInTag({
				  tag: 'strong',
				  words: [search_text]
				});
	    },
	    type: 'POST'
	});

  }
}	

$('.search-term').keydown(function(e) {
      if (e.keyCode == 13) {
    postSearchFunction();
    }
 });

$("#dell-search-button").click(function(){
  postSearchFunction();
});

$(document).on('click', '.doc_update_button', function(){
	if(user_privilege!=0){
	$(".add-new-div").hide();
	$(".delete-div").hide();
	$("#update-file-list").html("");
	$("#update-file-list").append($("#doc-file-list").html());
	$(".doc_id_no").val($("#doc_id_no2").text());
	$(".update-div #title").val($(this).parent().find("#main-post-tile").text().trim());
	$(".update-div #short_desc").val($(this).parent().find(".less-description").text().trim());
	$(".update-div #long_desc").val($("article#dw-doc-open").find(".more-description").text().trim());
	$(".update-div").show();
	}
	else{
		alert("Sorry!! you dont have enough privilege to update...");
	}
});

$("#update-form-close").click(function(){
	$(".update-div .input_title").val("");
	$(".update-div .input_short_desc").val("");
	$(".update-div .input_long_desc").val("");
	$(".update-div .input_file-upload").val("");
	$(".update-div").hide();
	});

$("#add-new-form-close").click(function(){
	$(".add-new-div .input_title").val("");
	$(".add-new-div .input_short_desc").val("");
	$(".add-new-div .input_long_desc").val("");
	$(".add-new-div .input_file-upload").val("");
	$(".add-new-div").hide();
	});

$("#add_new_button").click(function(){
	$(".delete-div").hide();
	$(".update-div").hide();
	$(".add-new-div").show();
	
	});
$("#delete-form-close").click(function(){
	$(".delete-div .input_title").val("");
	$(".delete-div").hide();
	});
$("#delete_button").click(function(){
	$(".delete-div").show();
	$(".update-div").hide();
	$(".add-new-div").hide();
	});
$("#dw-logout-button").click(function(){
	window.location.replace("logout");
});




  /*for the acknowledgement for the task eq-adding,updating*/
	var add_result=$(".add-result").text().trim();
	if(add_result.search("44")  > -1 ){
		alert("successfully done...");
		window.location.replace("home");
	}
	else if(add_result.search("55")  > -1 ){
		alert("sorry!! Title is not available...");
		window.location.replace("home");
	}
	else if(add_result.search("66")  > -1 ){
		alert("Failed!! Title is already present! Please update...");
		window.location.replace("home");
	}
	
/*for storing the file in es*/
    var file_value="";
    var all_file_value="";
    var all_file_name="";
    var handleFileSelect = function(evt) {
        var files = evt.target.files;
        all_file_value="";
        all_file_name="";
        for (var i = 0; i < files.length; i++)
        {  
        	var file= files[i];
        	handleOneFileSelect(file);
        }
    };
    function handleOneFileSelect(file){
        if (file) {
            var reader = new FileReader();
            reader.onload = function(readerEvt) {
                var binaryString = readerEvt.target.result;
                file_value = btoa(binaryString);
                all_file_name=all_file_name+file.name+";";
                all_file_value = all_file_value + file_value + ";";
            };
            reader.readAsBinaryString(file);
        }
    }
if (window.File && window.FileReader && window.FileList && window.Blob) {
            var el = document.querySelectorAll('.input_file-upload');
            for(var i=0; i < el.length; i++){
                el[i].addEventListener('change', handleFileSelect, false);
            }
} else {
    alert('The File APIs are not fully supported in this browser.');
}

$('.input_file-upload').on('change',function(){
    
    $(".loading-anime-image").show();
    setTimeout(function () { $('.file-contents-stream').val(all_file_value);
    $('.input_file-detail').val(all_file_name);
    file_value="";
    setTimeout(function () { $(".loading-anime-image").hide();}, 500);}, 1000);  
});	
});


/*for checking session expired or not*/
//function CheckUserSession() {
//	$.ajax({
//	    url: 'home',
//	    success:function(data){
//	    	if(data.search("dw-login-form")>0)
//	    		{
//	    		window.location.replace("home");
//	    		}
//	    },
//	    type: 'POST'
//	});
//}
//
//$("body").click(function(){
//	CheckUserSession();
//});

/*for the pagination*/
function executePagination(){	
$('#pagination-demo').remove();
$("#topic-list").after("<ul id='pagination-demo' class='pagination-sm'></ul>");
var k=1;
$(".topic").each(function() {
	$(this).hide();
	if(k>=1 && k<=10)$(this).show();
   	else $(this).hide();
  	k++;
});
$('#pagination-demo').show();

var p_ttlPages;
if($(".topic").length % 10 == 0){
	p_ttlPages = parseInt($(".topic").length/10);	
}
else{
	p_ttlPages = parseInt($(".topic").length/10) + 1;
}
var p_vpages=1;
if(p_ttlPages>10) p_vpages=10;
else if(p_ttlPages>1 && p_ttlPages<10) p_vpages=p_ttlPages;
$('#pagination-demo').twbsPagination({
        totalPages: p_ttlPages,
        visiblePages: p_vpages,
        onPageClick: function (event, page) {
      	var i=0;
        $(".topic").each(function(){
       	if(i>=((page-1)*10) && i<=((page)*10))$(this).show();
       	else $(this).hide();
      	i++;
     });
        $('html, body').animate({scrollTop: '200px'},200 );
   }
});
if(p_vpages==1){
   	$('#pagination-demo').hide();
   }
}
/*end for pagination*/
$("#dw-user-name").text($("#dw-user-id").text());
