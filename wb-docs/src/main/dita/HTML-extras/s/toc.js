<!--

/*  **********************************************************  */
/*  For inclusion in the HEAD of each "table-of-contents" page  */
/*  **********************************************************  */

/*  **********  */
/*  Background  */

/*  A table-of-contents page exists to present a multi-level unnumbered 
    list. A list item that has a list nested beneath it is called a "folder" 
    item. If a list item, including a folder, contains an A tag to link to a 
    document page, it is called a "page" item. 
    
    It is important that sections of the list can be expanded and collapsed 
    at the user's direction. These changes to the visual rendering of the 
    list are eased by cooperation with a style sheet. In particular, this 
    script is to assign folder items to one of two classes that indicate the 
    expanded or collapsed state.  */

/*  *************  */
/*  Configuration  */

/*  ID for TOC root  */

var sTocRoot = "Root";

/*  Class names for LI elements  */

var sCollapsedClass = "Collapsed";
var sExpandedClass ="Expanded";
var sFolderClass = "Folder";
var sPageClass = "";
var sUnconfiguredClass = "";

/*  Corresponding images  */

var sCollapsedImage = "/_images/collapsed.gif";
var sExpandedImage = "/_images/expanded.gif";
var sFolderImage = "/_images/folder.gif";
var sPageImage = "/_images/page.gif";

/*  Class names for A elements  */

var sCurrentLink = "Current";
var sOtherLink = "";

/*  ****  */
/*  Root  */

var oTocRoot = null;

function GetTocRoot ()
{
    if (oTocRoot != null) return oTocRoot;

    /*  Ordinarily, find the TOC root by ID.  */

    var document = window.document;
    var root = document.getElementById (sTocRoot);
    if (root != null && root.tagName == "UL") return oTocRoot = root;

    /*  If the author has forgotten to label a TOC root, find it as the 
        first UL that is a direct descendant of the body.  */

    var children = document.body.childNodes;
    var count = children.length;
    for (var n = 0; n < count; n ++) {
        var x = children [n];
        if (x.tagName == "UL") return x;
    }
    return null;
}

/*  *****  */
/*  Links  */

/*  For the purposes of the TOC, a link is an A tag that has an HREF and 
    lies in an LI tag (which is then a page item).  */

/*  Each time the TOC loads, one link becomes current, namely the one that 
    represents whatever page is loaded into the document frame.  */

var CurrentLink = null;

/*  The SetCurrentLink function is to be called from the onload handler once 
    it is known which link matches whatever page is being shown in the 
    document frame.  */

function SetCurrentLink (Link)
{
    if (CurrentLink != null) CurrentLink.className = sOtherLink;
    (CurrentLink = Link).className = sCurrentLink;

    /*  The current link does not always have the focus, but it does always 
        start with the focus.  */

    if (CurrentLink != window.document.activeElement) CurrentLink.focus ();
}

/*  Let's also distinguish the very first link, if only for use as a 
    default.  */

var HomeLink = null;

/*  If only as a temporary measure, let's also count the links.  */

var nLinks = 0;

/*  The GetDocumentLink function searches the TOC for a link to the given 
    document page. Allow null as the argument, as a shorthand for whatever 
    document is the TOC's first link.  */

function FindDocumentInLinks (Links, Doc)
{
    var numlinks = Links.length;
    for (var n = 0; n < numlinks; n ++) {
        var a = Links [n];
        if (a.href == Doc) return a;
    }
    return null;
}

function GetDocumentLink (Doc)
{
    /*  If the given document page is already a known link, then we can save 
        some trouble.  */

    if (CurrentLink != null && Doc == CurrentLink.href) return CurrentLink;
    if (HomeLink != null) {
        if (Doc == HomeLink.href || Doc == null) return HomeLink;
    }

    /*  Ordinarily, search all the links in the TOC.  */
    
    var root = GetTocRoot ();
    if (root == null) return null;

    var links = root.getElementsByTagName ("A");
    if (links.length == 0) return null;

    if (HomeLink == null) HomeLink = links [0];
    if (Doc == null) return HomeLink;

    var a = FindDocumentInLinks (links, Doc);
    if (a != null) return a;

    /*  Although every link from within the site should specify a file, links 
        from outside may specify a directory, expecting the site to add a 
        default filename. Given a link that doesn't match any in the TOC, it 
        perhaps does not hurt to add the default filename and retry.  */

    return FindDocumentInLinks (links, PathAppend (Doc, sDefaultFilename));
}

/*  **********  */
/*  List items  */

/*  Given any HTML element, return the list item that either is the element 
    or is its tightest container.  */

function GetListItem (Element)
{
    for (var x = Element; x != null; x = x.parentNode) {
        if (x.tagName == "LI") return x;
    }
    return null;
}

/*  Given a list item that links to a page, return the link (which is the 
    first A among the item's children before the first UL).  */

function GetItemLink (Item)
{
    var children = Item.childNodes;
    var count = children.length;
    for (var n = 0; n < count; n ++) {
        var x = children [n];
        switch (x.tagName) {
            case "A": return x;
            case "UL": return null;
        }
    }
    return null;
}

/*  =======================  */
/*  List-Item Image Caching  */

/*  It is extremely undesirable that the browser might seek to download over 
    and over the same handful of tiny image files that we use to distinguish 
    list items. Yet observation of Internet Explorer sometimes shows such 
    repetition, even though the browser is configured for caching and is 
    seen to cache image files that are named explicitly in IMG elements. 

    A solution appears to come from creating a so-called off-screen image 
    element for each marker. Then, it seems, the browser keeps getting the 
    image from the Image. It would be good to know more about this.  */

var aoListItemImages = new Array ();

function CacheListItemImage (Class, Src)
{
    var src = new LocalUrl (Src).toString ();
    var img = window.document.createElement ("IMG");
    img.src = src;
    aoListItemImages.push (img);
    return src;
}

/*  *****************  */
/*  Style Sheet Rules  */

function SetListItemImage (Sheet, Class, Src)
{
    var src = CacheListItemImage (Class, Src);
    var selector = Class == "" ? "UL" : "LI" + "." + Class;
    var rule = "list-style-image:url('" + src + "')";
    return AppendStyleSheetRule (Sheet, selector, rule);
}

function SetFolderVisibility (Sheet, Class, Display)
{
    var selector = "LI";
    if (Class != null && Class != "") selector += "." + Class;
    selector += " UL";
    var rule = "display:" + (Display ? "block" : "none");
    return AppendStyleSheetRule (Sheet, selector, rule);
}

function SetListItemStyles ()
{
    var sheet = GetLastStyleSheet ();
    if (sheet == null) return false;

    if (SetFolderVisibility (sheet, sUnconfiguredClass, false)
            && SetFolderVisibility (sheet, sFolderClass, false)
            && SetFolderVisibility (sheet, sExpandedClass, true)
            && SetFolderVisibility (sheet, sCollapsedClass, false)) {

        if (SetListItemImage (sheet, sPageClass, sPageImage)
                && SetListItemImage (sheet, sFolderClass, sFolderImage)
                && SetListItemImage (sheet, sExpandedClass, sExpandedImage)
                && SetListItemImage (sheet, sCollapsedClass, sCollapsedImage)) {

            return true;
        }
    }
    return false;
}

/*  *****  */
/*  Focus  */

function EnsureFocusForFolder (Folder)
{
    /*  If the folder has a link, presume that the link can already receive 
        the focus. Otherwise, wrap all the folder's text into a SPAN and set 
        the SPAN as a tab stop.  */

    var a = GetItemLink (Folder);
    if (a == null) {
        var first = Folder.firstChild;
        if (first.tagName != "UL") {
            var span = window.document.createElement ("SPAN");
            var x = first;
            do {
                var y = x.nextSibling;
                span.appendChild (Folder.removeChild (x));
                x = y;
            } while (x.tagName != "UL");
            Folder.insertBefore (span, x);
            span.tabIndex = 0;
        }
    }
}

function SetFocusForFolder (Folder)
{
    /*  Set the focus to the given folder, i.e., to its link or to the SPAN 
        we confected for its link-less text.  */

    var x = GetItemLink (Folder);
    if (x == null) {
        x = Folder.firstChild;
        if (x.tagName != "SPAN" || x.tabIndex == null) return;
    }
    x.focus ();
}

/*  *******  */
/*  Folders  */

/*  To help with saving and restoring the expansion state of the list, every 
    folder gets numbered. And while numbering the folders, we are as well to 
    build a record of relationships among folders. 

    The following arrays are indexed by a folder number and provide 
    respectively the corresponding object, the folder number of that 
    object's parent folder, and an array of folder numbers of that object's 
    child folders. 

    Once initialised, these arrays each have as many members as there are 
    folders. Given a folder number that is known to be in range, the 
    corresponding member may be assumed to be defined. The only applicable 
    validity check is that the array of parent numbers has a null member for 
    each folder that has no parent.  */

var aoFolder = new Array ();
var aiParent = new Array ();
var aaiChildren = new Array ();

/*  Two more arrays, also indexed by folder number, provide respsectively 
    the folder number of the first and last folders among the descendants. 

    Given a folder number that is known to be in range, the corresponding 
    member in each array is undefined if the folder has no descendants, but 
    may otherwise be assumed to be a valid folder number.  */

var aiFirstDescendant = new Array ();
var aiLastDescendant = new Array ();

/*  All folder initialisation is done in the one function, following. It is 
    assumed here that folders are initialised in their order of appearance 
    in the TOC document, and particularly that parents are initialised 
    before their children.  */

function InitFolder (Folder)
{
    var i = aoFolder.length;

    /*  Create a new property for the folder's ID. This gives a relatively 
        quick way of finding the number again when given the folder.  */

    Folder.foldernumber = i;

    /*  Enter the folder into an array indexed by the number. This gives a 
        very quick way of finding the folder again when given the number.  */

    aoFolder [i] = Folder;

    /*  Initialise a list of child folders, it being easier later to know 
        that all numbered folders have such a list, even if empty.  */

    aaiChildren [i] = new Array ();

    /*  Map the folder to its parent. If there is a parent, add the newly 
        numbered folder to the parent's list of children.  */

    var x = Folder;
    while ((x = x.parentNode) != null && x.tagName != "LI") {
    }
    if (x != null) {
        var j = x.foldernumber;
        aiParent [i] = j;
        aaiChildren [j].push (i);

        /*  It perhaps does not cost very much if we also track for each 
            ancestor the range of folder numbers for its descendants. This 
            allows the "full" expansion of folders a faster implementation 
            without recursion.  */

        if (aiFirstDescendant [j] == null) {
            aiFirstDescendant [j] = aiLastDescendant [j] = i;
        }

        do {
            if (i > aiLastDescendant [j]) aiLastDescendant [j] = i;
        } while ((j = aiParent [j]) != null);
    }
    else {
        aiParent [i] = null;
    }

    /*  Ensure that every folder is capable of receiving the focus.  */

    EnsureFocusForFolder (Folder);
}

function InitFolders ()
{
    /*  Work through the TOC's list items to identify the folder items and 
        initialise them (which means to number them and to build whatever 
        information we mean to keep, for efficiency, about relationships
        between them).  */

    var root = GetTocRoot ();
    if (root == null) return;

    var list = root.getElementsByTagName ("LI");
    var count = list.length;
    for (var n = 0; n < count; n ++) {
        var li = list [n];

        if (GetItemLink (li) != null) nLinks ++;

        /*  A list item is a folder item if it has a list beneath it.  */

        if (li.getElementsByTagName ("UL").length != 0) InitFolder (li);
    }
}

/*  Given any HTML element, if the corresponding list item is a folder, 
    return the folder number.  */

function GetElementFolder (Element)
{
    var li = GetListItem (Element);
    return li != null ? li.foldernumber : null;
}

/*  Given any HTML element, return the corresponding list item's closest 
    ancestor that is a folder. This is the "enclosing" folder that would 
    have to be expanded if the given element is to be seen.  */

function GetEnclosingFolder (Element)
{
    var li = GetListItem (Element);
    if (li == null) return null;

    /*  If the corresponding list item is a folder, we know its parent.  */

    var i = li.foldernumber;
    if (i != null) return aiParent [i];

    /*  Otherwise, find the closest ancestor that is a folder.  */

    for (var x = li.parentNode; x != null; x = x.parentNode) {
        if (x.tagName == "LI") return x.foldernumber;
    }
    return null;
}

/*  ================  */
/*  Folder Expansion  */

/*  A folder can at any time be in one of three expansion states: namely, 
    expanded, collapsed or undefined. A folder is also at any time either 
    visible or not. However, the combination of undefined with visible is 
    not permitted. 

    Transitions are performed by two operators. The "expand" and "collapse" 
    operators change a folder from any state to the expanded and collapsed 
    state, respectively. The folder's visibility does not change. Visibility 
    of the folder's child items is affected non-trivially. 

    Under the "expand" operator, every child item becomes visible. A child 
    item that has the expanded or collapsed state keeps that state, but 
    undefined changes to collapsed. Under the "collapsed" operator, every 
    child item becomes invisible. No child item changes its expansion state. 

    One folder, here called the top folder, is the ancestor of all others. 
    In the initial state, this folder is collapsed and visible.  */

var abExpanded = new Array ();

function SetFolderExpansion (FolderNumber, Expand)
{
    var f = aoFolder [FolderNumber];
    if (f == null) return;

    if (abExpanded [FolderNumber] == Expand) return;

    abExpanded [FolderNumber] = Expand;

    var classname = Expand ? sExpandedClass : sCollapsedClass;
    if (f.className != classname) f.className = classname;
}

function ExpandFolder (FolderNumber)
{
    /*  Since expansion of a visible folder makes child folders visible, 
        first ensure that every child folder has a well-defined expansion 
        state. Default to collapsed so that a first expansion expands only 
        one level.  */

    var children = aaiChildren [FolderNumber];
    for (var n in children) {
        if (IsBackFittedArrayMethod (n)) continue;

        var i = children [n];
        if (abExpanded [i] == null) SetFolderExpansion (i, false);
    }
    SetFolderExpansion (FolderNumber, true);
}    

function CollapseFolder (FolderNumber)
{
    SetFolderExpansion (FolderNumber, false);
}

function ExpandFolderFull (FolderNumber)
{
    if (aiFirstDescendant [FolderNumber] != null) {
        var first = aiFirstDescendant [FolderNumber];
        var last = aiLastDescendant [FolderNumber];
        for (var i = first; i <= last; i ++) {
            SetFolderExpansion (i, true);
        }
    }
    SetFolderExpansion (FolderNumber, true);
}

function CollapseFolderFull (FolderNumber)
{
    SetFolderExpansion (FolderNumber, false);

    if (aiFirstDescendant [FolderNumber] != null) {
        var first = aiFirstDescendant [FolderNumber];
        var last = aiLastDescendant [FolderNumber];
        for (var i = first; i <= last; i ++) {
            SetFolderExpansion (i, false);
        }
    }
}

/*  ============================  */
/*  Folder Expansion Persistence */

function BuildStateString (State, Filter)
{
    var str = null, first = null, last = null;
    for (var i in State) {
        if (IsBackFittedArrayMethod (i)) continue;

        if (State [i] != Filter) continue;

        i = parseInt (i);
        if (str == null) {
            first = last = i;
            str = first;
        }
        else if (i == last + 1) {
            last = i;
        }
        else {
            if (first != last) {
                str += (last == first + 1 ? "," : "-") + last;
            }
            first = last = i;
            str += "," + first;
        }
    }
    if (first != null && first != last) {
        str += (last == first + 1 ? "," : "-") + last;
    }
    return str;
}

function ComposeTocExpansionArgumentValue (Doc)
{
    var state = new Array ();
    var gotexpanded = false;
    var gotcollapsed = false;
    var i;

    /*  For each folder whose expansion state is not (still) undefined, 
        check whether any child folder is expanded.  */

    for (i in abExpanded) {
        if (IsBackFittedArrayMethod (i)) continue;

        var hasexpandedchild = false;
        var children = aaiChildren [i];
        for (n in children) {
            if (IsBackFittedArrayMethod (n)) continue;

            if (abExpanded [children [n]]) hasexpandedchild = true;
        }

        /*  If a folder is expanded but has at least one expanded child, 
            then the folder's expanded state can be regenerated from the 
            child, and needn't be recorded. 

            The opposite applies if a folder is collapsed but has any 
            expanded child. The collapsed state must be recorded, if only 
            to stop the false regeneration of an expanded state from the 
            child.  */
            
        if (abExpanded [i]) {
            if (!hasexpandedchild) {
                state [i] = true;
                gotexpanded = true;
            }
        }
        else {
            if (hasexpandedchild) {
                state [i] = false;
                gotcollapsed = true;
            }
        }
    }

    /*  The ancestor folders of the given document can all be known from the 
        document, and need not be recorded.  */

    var a = GetDocumentLink (Doc);
    if (a != null) {
        for (i = GetEnclosingFolder (a); i != null; i = aiParent [i]) {
            delete state [i];
        }
    }

    /*  Build comma-separated descriptions of the expanded and collapsed 
        folders. Separate these two with a semicolon.  */

    var xstr = gotexpanded ? BuildStateString (state, true) : null;
    if (xstr == null) return null;
    var cstr = gotcollapsed ? BuildStateString (state, false) : null;
    return cstr != null ? xstr + ";" + cstr : xstr;
}

var sTocInitExpand = null;
var sTocInitCollapse = null;

function ParseTocExpansionArgumentValue (Str)
{
    var parts = Str.split (";");
    if (parts.length > 0) {
        sTocInitExpand = parts [0];
        if (parts.length > 1) {
            sTocInitCollapse = parts [1];
        }
    }
}

function ParseTocInitString (Str, State, Expand)
{
    /*  The fundamental elements of the string are folder numbers, and there 
        is a known maximum by the time this function executes.  */

    var numfolders = aoFolder.length;

    /*  The string is a sequence of ranges separated by commas. Each range 
        is either a single folder number or a pair (first and last) 
        separated by a hyphen.  */

    var args = Str.split (",");
    for (var n in args) {
        if (IsBackFittedArrayMethod (n)) continue;

        var range = args [n].split ("-");
        if (range.length > 0) {

            /*  Extract the folder numbers from the range, and validate 
                them. Treat a single folder number as a trivial pair.  */

            var first = parseInt (range [0]);
            if (!(0 <= first && first < numfolders)) return null;
            var last = first;
            if (range.length > 1) {
                last = parseInt (range [1]);
                if (!(0 <= last && last < numfolders)) return null;
                if (range.length > 2) return null;
            }

            for (var i = first; i <= last; i ++) {
                State [i] = Expand;
            }
        }
    }
    return State;
}

function InitTocExpansion (Doc)
{
    CollapseFolder (0);

    var a = GetDocumentLink (Doc);
    if (a == null) return HomeLink;

    var state = new Array ();
    if (sTocInitExpand != null) {
        state = ParseTocInitString (sTocInitExpand, state, true);
        if (state != null && sTocInitCollapse != null) {
            state = ParseTocInitString (sTocInitCollapse, state, false);
        }
    }
    if (state == null) state = new Array ();

    var i = GetEnclosingFolder (a);
    if (i != null) state [i] = true;

    for (i in state) {
        if (IsBackFittedArrayMethod (i)) continue;

        if (state [i]) {

            ExpandFolder (i);

            var j = i;
            while ((j = aiParent [j]) != null && state [j] == null) {
                if (!abExpanded [j]) ExpandFolder (j);
            }
        }
        else {
            CollapseFolder (i);
        }
    }

    /*  Always expand the top level.  */

    ExpandFolder (0);

    return a;
}

/*  *********  */
/*  Scrolling  */

function ScrollIntoView (Element)
{
    var body = Element.offsetParent;

    var xt = Element.offsetTop;
    var xb = xt + Element.offsetHeight;

    var bt = body.scrollTop;
    var bb = bt + body.clientHeight;

    /*  The cases that are most easily dealt with (and may be anyway the 
        most common in practice) have the link at least partly in view. Get 
        to these first.  */

    if (xb <= bb) {

        if (xt >= bt) {

            /*  The whole element is already visible in the client area. Do 
                nothing.  */

            return;
        }
        else if (xb >= bt) {

            /*  The element is partly visible at the top of the client area. 
                A little move is all that's needed.  */

            body.scrollTop = xt;
            return;
        }
    }
    else {
        if (xt <= bb) {

            /*  The element is partly visible at the bottom of the client 
                area. This doesn't seem like good cause for much change. 
                Just nudge the scrolling enough to make the element wholly 
                visible.  */

            body.scrollTop = bt + (xb - bb);
            return;
        }
        else if (xt < bt) {

            /*  The element straddles the client area. This case is not 
                anticipated in practice - at least, not while the element is 
                expected to be just a link, which is in all typical use of 
                negligible height relative to the TOC frame.  */

            return;
        }
    }

    /*  The element is wholly outside the client area. Bringing it into 
        view is unavoidably a large change in appearance. Seek some sort of 
        natural boundary.  */

    bt = xt;
    for (var i = GetEnclosingFolder (Element); i != null; i = aiParent [i]) {
        var ft = aoFolder [i].offsetTop;
        if (xt > ft + body.clientHeight) break;
        bt = ft;
    }
    body.scrollTop = bt;
}

/*  ========================  */
/*  Scroll State Persistence  */

function ComposeTocScrollArgumentValue ()
{
    var body = window.document.body;
    if (body == null) return null;

    var x = body.scrollLeft;
    var y = body.scrollTop;

    if (x == 0 && y == 0) return null;

    return x + "," + y;
}

var TocScrollX = 0;
var TocScrollY = 0;

function ParseTocScrollArgumentValue (Str)
{
    var args = Str.split (",");
    if (args.length > 0) {
        TocScrollX = args [0];
        if (args.length > 1) {
            TocScrollY = args [1];
        }
    }
}

function SetTocScrollState ()
{
    /*  A quirk is worth noting in this otherwise straightforward function: 
        correct restoration seems to occur reliably only if the scroll 
        properties are read before they are written.  */

    var body = document.body;
    if (TocScrollX != 0 && body.scrollLeft != TocScrollX) {
        body.scrollLeft = TocScrollX;
    }
    if (TocScrollY != 0 && body.scrollTop != TocScrollY) {
        body.scrollTop = TocScrollY;
    }
}

/*  ***********  */
/*  Frame Width  */

var ContainingFrameSet = null;
var InitialTocWidth = 0;

function GetTocWidth ()
{
    var widths = ContainingFrameSet.cols.split (",", 3);
    if (widths.length != 2) return null;

    var total = ContainingFrameSet.offsetWidth;

    /*  If the TOC width is given as a percentage, convert it, both for our 
        result and for how it is stored in the frameset element.  */

    var fields = widths [0].split ("%", 2);
    if (fields.length == 2 && fields [1] == "") {

        /*  Since Opera is never observed to have a non-zero offsetWidth, 
            resizing can't be supported.  */

        if (total == 0) return null;

        widths [0] = Math.round (total * fields [0] / 100);
        widths [1] = total - widths [0];
        ContainingFrameSet.cols = widths.toString ();
    }
    return total != 0 && widths [0] < total ? widths [0] : null;
}

function SetTocWidth (Width)
{
    var widths = ContainingFrameSet.cols.split (",", 3);
    if (widths.length != 2) return;

    if (widths [0] == Width) return;

    var total = ContainingFrameSet.offsetWidth;

    var fields = Width.split ("%", 2);
    if (fields.length == 2 && fields [1] == "") {

        if (total == 0) return;

        Width = Math.round (total * fields [0] / 100);
    }

    if (widths [0] != Width && (total == 0 || Width < total)) {
        widths [0] = Width;
        if (total != 0) widths [1] = total - Width;
        ContainingFrameSet.cols = widths.toString ();
    }
}

function InitTocWidth ()
{
    var topdoc = window.top.document;
    var tocframe = topdoc.getElementById (sTocFrame);
    if (tocframe == null) return;
    ContainingFrameSet = tocframe.parentNode;
    InitialTocWidth = GetTocWidth ();
}

/*  =======================  */
/*  Frame Width Persistence  */

function ComposeTocWidthArgumentValue ()
{
    var width = GetTocWidth ();
    if (width == null || width == InitialTocWidth) return null;
    return escape (width);
}

function ParseTocWidthArgumentValue (Str)
{
    SetTocWidth (unescape (Str));
}

/*  ==================  */
/*  Frame Width Resize  */

function AutoResizeToc ()
{
    /*  All I can say in favour of the following calculation is that it 
        seems to work in a few test cases. 

        Note for another time: the "currentStyle" object is non-standard.  */

    var ul = aoFolder [0].parentNode;
    var listwidth = ul.offsetLeft + ul.scrollWidth;
    var body = window.document.body;
    var bodymargin = parseInt (body.currentStyle.marginRight);
    var excess = body.offsetWidth - body.clientWidth;
    var x = listwidth + bodymargin + excess;
    if (x != GetTocWidth ()) SetTocWidth (x);
}

/*  ***********  */
/*  Persistence  */

function ParseTocArguments (Args)
{
    var tx = Args.GetValue (sTocExpansionArgumentName);
    if (tx != null) ParseTocExpansionArgumentValue (tx);

    var ts = Args.GetValue (sTocScrollArgumentName);
    if (ts != null) ParseTocScrollArgumentValue (ts);

    var tw = Args.GetValue (sTocWidthArgumentName);
    if (tw != null) ParseTocWidthArgumentValue (tw);
}

/*  Public function - use only from MASTER.JS (see LocalUrl.prototype.View)  */

function ComposeTocArguments (Url)
{
    var args = new ParsedSearch ();

    /*  If the intended new document is not in the same subweb, it will have 
        a different table of contents, and some properties of the present 
        table of contents cannot sensibly persist.  */

    if (GetSubwebPath (Url.pathname) == window.top.locSubweb.pathname) {

        var tx = ComposeTocExpansionArgumentValue (Url.toString ());
        if (tx != null) args.AddArgument (sTocExpansionArgumentName, tx);

        var ts = ComposeTocScrollArgumentValue ();
        if (ts != null) args.AddArgument (sTocScrollArgumentName, ts);
    }

    var tw = ComposeTocWidthArgumentValue ();
    if (tw != null) args.AddArgument (sTocWidthArgumentName, tw);

    return args;
}

/*  ************  */
/*  Mouse Events  */

function TocExpansionOnClick (FolderNumber, Event)
{
    var alt = Event.altKey;
    var ctrl = Event.ctrlKey;
    var shift = Event.shiftKey;

    /*  A simple click (with none of Ctrl, Alt or Shift down) just toggles 
        the folder's expansion state.  */

    if (!ctrl && !alt && !shift) {
        abExpanded [FolderNumber] 
            ? CollapseFolder (FolderNumber) 
            : ExpandFolder (FolderNumber);
        return true;
    }
    
    /*  Hold the Ctrl key down to expand a folder and all its subfolders, 
        even if any of these are already expanded.  */

    if (ctrl && !alt && !shift) {
        ExpandFolderFull (FolderNumber);
        return true;
    }

    /*  Hold down Ctrl and Shift to collapse a folder and all its 
        subfolders, even if any of these are already collapsed.  */

    if (ctrl && !alt && shift) {
        CollapseFolderFull (FolderNumber);
        return true;
    }

    /*  Hold the Alt key down just to get the focus moved to the folder's 
        link (if it has one).  */

    return !ctrl && alt && !shift;
}

/*  Though Internet Explorer helpfully sets offsetX and offsetY members for 
    each element an event is sent to, these are not standard. Using the 
    clientX and clientY members seems to require that we locate the element 
    relative to the client window - done here by adding the offsets.  */

function GetElementLeft (Element)
{
    var left = Element.offsetLeft;
    while ((Element = Element.offsetParent) != null) {
        left += Element.offsetLeft;
    }
    return left;
}

function GetElementTop (Element)
{
    var top = Element.offsetTop;
    while ((Element = Element.offsetParent) != null) {
        top += Element.offsetTop;
    }
    return top;
}

function IsClickAboveChildList (FolderNumber, Event)
{
    if (abExpanded [FolderNumber]) {
        var ul = aoFolder [FolderNumber].getElementsByTagName ("UL") [0];
        if (Event.clientY >= GetElementTop (ul)) return false;
    }
    return true;
}

function FindClickedChildFolder (FolderNumber, Event)
{
    if (abExpanded [FolderNumber]) {
        var y = Event.clientY;
        var children = aaiChildren [FolderNumber];
        for (var n in children) {
            var i = children [n];
            var li = aoFolder [i];
            var top = GetElementTop (li);
            if (y < top) return null;
            var bottom = top + li.offsetHeight;
            if (y < bottom) {
                return IsClickAboveChildList (i, Event) ? i : null;
            }
        }
    }
    return null;
}

function GetClickedFolder (FolderNumber, Event)
{
    /*  Accommodating the various browsers is quite a headache. There are at 
        least three variant interpretations of the area in which a click 
        sends an "onclick" event to a list item. 

        Both Internet Explorer and Firefox have a list-item respond to 
        clicks outside the primary box. Firefox limits this just to the 
        secondary box for the list-item marker. 

        Opera keeps just to the primary box, which of course makes it very 
        difficult to detect that a click has occurred on the marker.  */

    /*  A click that's too far right has occurred in the folder's primary 
        box, not in its list-item marker.  */

    var li = aoFolder [FolderNumber];
    if (Event.clientX >= GetElementLeft (li)) {

        /*  In Internet Explorer and Firefox, that's the end of it. In 
            Opera, the list item that got the event may be the parent of the 
            list item whose marker has been clicked.  */

        return FindClickedChildFolder (FolderNumber, Event);
    }

    /*  For Firefox, that's the end of it. For Internet Explorer, require 
        that the click has occurred above any expanded list items.  */

    return IsClickAboveChildList (FolderNumber, Event) ? FolderNumber : null;
}

function OnClick (Event) 
{
    if (Event == null) Event = window.event;

    var i = null;
    for (var x = GetEventSource (Event); x != null; x = x.parentNode) {
        var tag = x.tagName;

        if (tag == "A") {

            /*  Given a click inside an A tag, including deeper inside (for 
                example, in a few characters of hyperlink text that happen 
                to be in italics), the desired behaviour is to follow the 
                link, which is just the default. However, it is more 
                efficient if the link is followed through the viewer.  */

            if (x.hostname == window.location.hostname) RedirectLocalLink (x);
            return;
        }
        else if (tag == "LI") {

            /*  Given a click inside a list item yet not actually on any 
                hyperlink within, check first whether the list item is a 
                folder and then whether the click occurred roughly near a 
                list-item marker.  */
                
            i = GetElementFolder (x);
            if (i != null) i = GetClickedFolder (i, Event);
            break;
        }
        else if (tag == "HTML") {

            /*  If the click is not dispatched to any LI element despite 
                having occurred roughly near the list-item marker for the 
                root folder, accept it as operating on the root folder.  */

            i = GetClickedFolder (0, Event);
            break;
        }
    }

    /*  If a folder was clicked, expand or collapse (depending on such 
        things as Ctrl and Shift keys.  */

    if (i != null) {
        if (TocExpansionOnClick (i, Event)) SetFocusForFolder (aoFolder [i]);
    }
    SetEventDone (Event);
}

/*  Though Microsoft's "HTML and Dynamic HTML Reference" doesn't say so (but 
    its "MSHTML Reference" does), the "onselectstart" event is available to 
    the document object. Defeat it here so that mouse clicks in combination 
    with the Ctrl key, such as we use for expanding and collapsing folders, 
    do not confuse by selecting text. 

    That said, the ability to select TOC text (most notably for copying to 
    the clipboard) might not be bad to have. Revisit this question some 
    time. 

    Anyway, this event isn't defined in any known standard and doesn't work 
    for Firefox.  */

function OnSelectStart (Event)
{
    if (Event == null) Event = window.event;

    SetEventDone (Event);
}

/*  ========  */
/*  Tooltips  */

/*  Some TOC items have long text, which gets truncated at the frame 
    boundary. To save scrolling, set a title for the browser to show as a 
    tooltip.  */

function IsLinkVisibilityPartial (Link)
{
    var xl = Link.offsetLeft;
    var xr = xl + Link.offsetWidth;

    var parent = Link.offsetParent;
    var pl = parent.scrollLeft;
    var pr = pl + parent.clientWidth;

    return xl < pl || xr > pr;
}

function OnMouseOver (Event)
{
    if (Event == null) Event = window.event;

    for (var x = GetEventSource (Event); x != null; x = x.parentNode) {
        if (x.tagName == "A") {
            if (IsLinkVisibilityPartial (x)) {
                var title = GetInnerText (x);
                if (title != null) x.title = title;
            }
            break;
        }
    }
}

function OnMouseOut (Event)
{
    if (Event == null) Event = window.event;

    for (var x = GetEventSource (Event); x != null; x = x.parentNode) {
        if (x.tagName == "A" && x.title != "") {
            if (IsLinkVisibilityPartial (x)) x.title = "";
        }
    }
}

/*  ***************  */
/*  Keyboard events  */

function TocExpansionOnKeyDown (Event, Key)
{
    var src = GetEventSource (Event);

    /*  Act on whatever folder has the focus:

            keypad +        expand folder 
            keypad -        collapse folder 
            keypad *        expand folder and subfolders 
            keypad /        collapse folder and subfolders  */

    var i = GetElementFolder (src);
    if (i == null) {

        /*  As a trial extension when collapsing, if the focus is with a 
            list item that is not a folder, collapse the enclosing folder.  */

        if (!(Key == 0x6D || Key == 0x6F)) return false;

        i = GetEnclosingFolder (src);
        if (i == null) return false;

        /*  Do not let the focus stay with a folder that becomes invisible.  
            Ordinarily, move the focus to the enclosing folder. If this has 
            no link, move the focus further up the tree.  */

        SetFocusForFolder (aoFolder [i]);
    }

    switch (Key) {
        case 0x6B: {                    // keypad +
            ExpandFolder (i);
            return true;
        }
        case 0x6D: {                    // keypad -
            CollapseFolder (i);
            return true;
        }
        case 0x6A: {                    // keypad *
            ExpandFolderFull (i);
            return true;
        }
        case 0x6F: {                    // keypad /
            CollapseFolderFull (i);
            return true;
        }
    }
    return false;
}

function OnKeyDown (Event)
{
    if (!IsRecognisedHostName ()) return;

    if (Event == null) Event = window.event;
    var key = Event.keyCode;
    switch (key) {

        /*  The keypad plus, minus, multiply and divide act to expand and 
            collapse the TOC in various ways.  */

        case 0x6B:      // keypad +
        case 0x6D:      // keypad - 
        case 0x6A:      // keypad *
        case 0x6F: {    // keypad /

            if (!Event.ctrlKey && !Event.altKey && !Event.shiftKey) {
                if (TocExpansionOnKeyDown (Event, key)) break;
            }
            return;
        }

        /*  If only for now, have Ctrl-Alt-R resize the TOC such that 
            horizontal scrollbars disappear.  */

        case 0x52: {    // R

            if (Event.ctrlKey && Event.altKey && !Event.shiftKey) {
                AutoResizeToc ();
                break;
            }
            return;
        }

        default: {
            return;
        }
    }
    SetEventDone (Event);
}

/*  ************************  */
/*  Load-Time Initialisation  */

function OnLoad (Event)
{
    InitFolders ();

    var doc = new LocalUrl (oViewerWindow.locDocument.pathname).toString ();
    var a = InitTocExpansion (doc);
    if (a != null) {
        SetTocScrollState ();
        ScrollIntoView (a);
        SetCurrentLink (a);
    }

    /*  If only as a temporary measure, show the number of pages and 
        folders.  */

    window.top.status = nLinks + " pages in " + aoFolder.length + " folders";
}

/*  *********************  */
/*  Global initialisation  */

var oViewerWindow = window.top;

/*  A table-of-contents page could be read in isolation, but not very 
    meaningfully. The page is meant to be seen through another page, called 
    a "viewer", which presents the page in a frame as context for a 
    document that is being shown concurrently in another frame. 

    Check that the window for this page is indeed a table-of-contents frame, 
    as expected. If it is not, then reload the whole window with the default 
    document from the same directory.  */

if (!IsViewerFrame (sTocFrame)) {
    if (IsRecognisedHostName ()) ViewDefaultFileInSameDirectory ();
}
else {

    var toc = oViewerWindow.locToc;
    if (toc != window.location.href) {
        window.location.replace (toc);
    }
    else if (IsRecognisedHostName ()) {

        SetListItemStyles ();
        HideNoScriptBlocks ();

        InitTocWidth ();
        ParseTocArguments (oViewerWindow.oUrlArguments);

        window.onload = OnLoad;

        window.document.onclick = OnClick;
        window.document.onkeydown = OnKeyDown;
        window.document.onmouseout = OnMouseOut;
        window.document.onmouseover = OnMouseOver;
        window.document.onselectstart = OnSelectStart;
    }
}

/*  Copyright © 2007-2009. Geoff Chappell. All rights reserved.  */

//-->