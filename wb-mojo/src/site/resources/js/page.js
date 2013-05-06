function showRuleDetails($rule) {

	$.get('templates/ruletemplate.html', function(data) {
		var $row = $rule;
		while ($row.nodeName.toLowerCase() != "tr") {
			$row = $row.parentNode;
		}

		var children = $row.children;
		$('.ruleDetails').html(data);
		var i;
		for (i = 0; i < children.length; i += 1) {
			if (children[i].nodeName.toLowerCase() == "input") {
				if (children[i].name.toLowerCase() == "ruleuuid") {
					$("#ruleuuid").html(children[i].value);
				} else if (children[i].name.toLowerCase() == "packagename") {
					$("#packagename").html(children[i].value);
				} else if (children[i].name.toLowerCase() == "description") {
					$("#description").html(children[i].value);
				} else if (children[i].name.toLowerCase() == "name") {
					$("#name").html(children[i].value);
				} else if (children[i].name.toLowerCase() == "severity") {
					$("#severity").html(children[i].value);
				} else if (children[i].name.toLowerCase() == "packageurl") {
					$("#packageurl").html(children[i].value);
				} else if (children[i].name.toLowerCase() == "ditauid") {
					$("#ditauid").html(children[i].value);
				} else if (children[i].name.toLowerCase() == "rulecode") {
					$("#rulecode").html(children[i].value);
				}
			}
		}
		$('html,body').animate({
			scrollTop : $(".ruleDetails").offset().top
		}, 1000);
		$('.findings').css("visibility", "hidden");
		$('.findings').css("height", "0");
	}, "html");

}

function showFindings(findigFile) {
	$('.findings').load(findigFile, function() {
		$('.findings').css("visibility", "visible");
		$('.findings').css("height", "100%");
		if($(".findings tr").size() > 25){
			$('.findings').css("height", "600px");
			$('.findings').css("overflow-x", "scroll");
		}
		$('html,body').animate({
			scrollTop : $(".findings").offset().top
		}, 1000);
	});
}

var callbackTest = {
	calculateTotalRating : function(opts) {
		var trs;
		if (opts.table == null) {
					trs = document.getElementById(opts).getElementsByTagName(
							"tbody")[0].rows, tot = 0, cnt = 0;
		} else {
			trs = document.getElementById(opts["table"]).getElementsByTagName(
					"tbody")[0].rows, tot = 0, cnt = 0;
		}
		for ( var i = 0, tr; tr = trs[i]; i++) {
			// If the row is visible i.e. has not display:none or the className
			// "invisibleRow" (used by the fake filter)
			if (tr.style.display != "none"
					&& tr.className.search(/(^|\s)invisibleRow($|\s)/) == -1) {
				tot += Number(fdTableSort.getInnerText(tr.cells[6]));
				cnt++;
			}
			;
		}
		;

	},
	displayTextInfo : function(opts) {
		if (!("currentPage" in opts)) {
			return;
		}
		var p = document.createElement('p'), t = document
				.getElementById(opts.table.concat('-fdtablePaginaterWrapTop')), b = document
				.getElementById(opts.table
						.concat('-fdtablePaginaterWrapBottom'));
		p.className = "paginationText";
		p.appendChild(document.createTextNode("Showing page "
				+ opts.currentPage + " of "
				+ Math.ceil(opts.totalRows / opts.rowsPerPage)));
		t.insertBefore(p.cloneNode(true), t.firstChild);
		b.appendChild(p);
	}
};

function linkme() {
}