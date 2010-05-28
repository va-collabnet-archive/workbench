<!--

/*  *******************************************************************  */
/*  For inclusion as first script in the HEAD of every displayable page  */
/*  *******************************************************************  */

/*  This script is common to all pages in the web. Different types of page 
    include different other scripts. This script must set no event 
    handlers.  */

/*  *************  */
/*  Configuration  */

/*  The site is intended to be experienced through a "viewer" page which 
    arranges that each "document" page is presented in some context, most 
    notably with a table of contents.  */

/*  ====================  */
/*  Window Relationships  */

/*  The viewer is always the page in the top window. When the viewer is in 
    the top window, it gives the top window a particular name.  */

var sViewerWindow = "Viewer for www.geoffchappell.com Site";

/*  Other pages are shown in frames. 

    The first frameset divides the top window into rows. The first row is 
    one frame, specifically for a banner, loaded from a "banner" page for 
    the whole site. 
    
    The second row is typically the bulk of the top window. It is in turn a 
    frameset, dividing this main area into columns. The first, typically 
    narrower, column shows a table of contents (TOC). The second, which is 
    the main presentation area, is for the site's many documents. 

    Current imagination is that a third row might show search results.  */

var sBannerFrame = "banner";
var sDividerFrame = "divider";
var sDocumentFrame = "doc";
var sTocFrame = "toc";

/*  =====  */
/*  Sites  */

/*  In an ideal world, redirection through the viewer would be unaffected by 
    a change of hostname. However, if a page gets reproduced at another 
    site, it will likely not be just the hostname that changes but the 
    pathname also. This is particularly a problem with caching by search 
    engines. 
    
    If only for now, defend against this by not letting the redirection 
    proceed if the hostname isn't recognised. 

    In an ideal world, the two grouped expressions would each begin with ?: 
    so that the match would not be saved. However, Internet Explorer 5.0 
    objects to it.  */

var aRecognisedHostNames = new Array (
    /(.+\.)*geoffchappell(\..+)*/i
);

/*  ===============  */
/*  Paths and Files  */

/*  The site is presented as a collection of subwebs. Though each may 
    actually be implemented as a subweb, the sense intended here is that 
    each has its own table of contents. The following is the definitive list 
    of subwebs. All the paths must be absolute. Where one of the paths is 
    deeper than another, the deeper must appear first. The root must be in 
    the list.  */

var asSubwebPaths = new Array (
    "/notes",
    "/studies/windows/km",
    "/studies/windows/win32",
    "/studies/windows/shell",
    "/studies/windows/ie",
    "/studies/msvc",
    "/"
);

/*  The site presently has one viewer, only in the root, but allow for more. 
    Again, all the paths must be absolute. Where one of the paths is deeper 
    than another, the deeper must appear first. The root must be in the 
    list.  */

var asViewerPaths = new Array (
    "/"
);

var sTocFilename = "toc.htm";
var sViewerFilename = "viewer.htm";

var sDefaultFilename = "index.htm"

/*  ============================  */
/*  Search string argument names  */

var sBookmarkArgumentName = "bm"
var sDocumentArgumentName = "doc";
var sTocScrollArgumentName = "ts";
var sTocWidthArgumentName = "tw";
var sTocExpansionArgumentName = "tx";

/*  =======================  */
/*  Viewing without scripts  */

/*  Class name for anything (typically in a DIV) that is intended to be seen 
    only when scripts don't run  */

var sNoScriptClass = "NoScript";

/*  ***********************  */
/*  Backwards Compatibility  */

/*  If the JScript engine is not at least version 5.5, then the built-in 
    Array object will not have a method named push. Back-fit one. Though 
    these scripts do not at present use the pop method, implement it too, if 
    only for symmetry.

    This back-fitting is suspect, of course. One unwelcome consequence, 
    which troubles the TOC script in several places is that a for...in loop 
    produces "push" and "pop" as array elements.  */

var BackFittedArrayPush = false;

if (Array.prototype.push == null) {

    Array.prototype.push = function ()
    {
        /*  This implementation in script follows as closely as seems 
            possible the native-code implementation that would execute if 
            the user upgraded. (For reference, in JSCRIPT.DLL, see 
            JsArrayPush). */

        var i, j, n = arguments.length;
        for (i = this.length, j = 0; j < n; i ++, j ++) {
            this [i] = arguments [j];
        }
        return i;
    };

    BackFittedArrayPush = true;
}

var BackFittedArrayPop = false;

if (Array.prototype.pop == null) {

    Array.prototype.pop = function ()
    {
        /*  This implementation in script follows as closely as seems 
            possible the native-code implementation that would execute if 
            the user upgraded. (For reference, in JSCRIPT.DLL, see 
            JsArrayPop). */

        var last = null;
        var n = this.length;
        if (n != 0) {
            last = this [n - 1];
            this [n - 1] = null;
            this.length = n - 1;
        }
        return last;
    };

    BackFittedArrayPop = true;
}

function IsBackFittedArrayMethod (Member)
{
    switch (Member) {
        case "push": return BackFittedArrayPush;
        case "pop": return BackFittedArrayPop;
    }
    return false;
}

/*  ******************  */
/*  Browser Variations  */

/*  Various work items must be done differently for different browsers.  */

function GetEventSource (Event)
{
    var src = Event.srcElement;
    return src != null ? src : Event.target;
}

function SetEventDone (Event)
{
    if (Event.stopPropagation != null) {
        Event.stopPropagation ();
        Event.preventDefault ();
    }
    else {
        Event.cancelBubble = true;
        Event.returnValue = false;
    }
}

function GetInnerText (Element)
{
    var text = Element.innerText;
    if (text != null) return text;

    text = "";
    var children = Element.childNodes;
    var count = children.length;
    for (var n = 0; n < count; n ++) {
        var x = children [n];
        switch (x.nodeType) {
            case 3: {
                text += x.nodeValue;
                break;
            }
            default: {
                text += GetInnerText (x);
                break;
            }
        }
    }
    return text;
}

function GetLastStyleSheet ()
{
    var sheets = window.document.styleSheets;
    var numsheets = sheets.length;
    return numsheets != 0 ? sheets [numsheets - 1] : null;
}

function AppendStyleSheetRule (Sheet, Selector, Style)
{
    if (Sheet.addRule != null) {
        Sheet.addRule (Selector, Style);
        return true;
    }
    else {
        var rule = Selector + "{" + Style + "}";
        var len = Sheet.cssRules.length;
        if (len == 0) return false;
        return Sheet.insertRule (rule, len) == len;
    }
}

function RegisterEventHandler (Target, EventName, Handler)
{
    if (Target.attachEvent != null) {
        Target.attachEvent ("on" + EventName, Handler);
    }
    else {
        Target.addEventListener (EventName, Handler, false);
    }
}

/*  ***************************  */
/*  URL Search String Arguments  */

/*  A search string may be appended to a page's URL to pass parameters to 
    the page. As parsed for the location.search property, it begins with a 
    question mark and is then followed by any number of arguments, 
    separated by & signs. Each argument has the form of a name and value, 
    separated at the first equals sign.  */

function ParsedSearch ()
{
    this.Names = new Array ();
    this.Values = new Array ();
    this.Count = 0;
}

ParsedSearch.prototype.AddArgument = function (Name, Value)
{
    if (Name == null || Name == "") return;
    if (Value == null || Value == "") return;

    this.Names.push (Name);
    this.Values.push (Value);

    this.Count ++;
}

ParsedSearch.prototype.AddString = function (Str)
{
    if (Str == null) return;

    var str = Str.charAt (0) == "?" ? Str.substr (1) : Str;
    var args = str.split ("&");
    for (var n = 0; n < args.length; n ++) {
        var nv = args [n].split ("=", 2);
        if (nv.length == 2) this.AddArgument (nv [0], nv [1]);
    }
}

ParsedSearch.prototype.AddParsed = function (Parsed)
{
    if (Parsed == null) return;

    var names = Parsed.Names;
    var values = Parsed.Values;
    var count = Parsed.Count;

    for (var n = 0; n < count; n ++) {
        this.AddArgument (names [n], values [n]);
    }
}

ParsedSearch.prototype.GetValue = function (Name)
{
    var names = this.Names;
    var count = this.Count;

    for (var n = 0; n < count; n ++) {
        if (names [n] == Name) return this.Values [n];
    }
    return null;
}

ParsedSearch.prototype.toString = function ()
{
    var count = this.Count;
    if (count == 0) return "";

    var args = new Array (count);
    var names = this.Names;
    var values = this.Values;
    for (var n = 0; n < count; n ++) {
        args [n] = names [n] + "=" + values [n];
    }
    return "?" + args.join ("&");
}

/*  ************  */
/*  Path Helpers  */

/*  Append the given name (of a file name or directory) or relative pathname 
    (again for either a file or directory) to the given path, which may be 
    either absolute or relative. Allow that the path may end with a slash, 
    and that the name may start with one. 

    This function assumes its arguments are non-null strings and it returns 
    a non-null non-empty string (containing at least one slash).  */

function PathAppend (Path, Name)
{
    var pathslash = Path.charAt (Path.length - 1) == "/";
    var nameslash = Name.charAt (0) == "/";

    if (pathslash || nameslash) return Path + Name;
    else if (pathslash && nameslash) return Path + Name.substr (1);
    else return Path + "/" + Name;
}

/*  Given a pathname (to a file or directory), extract the path (of the 
    containing directory). Allow that the pathname may end with a slash, 
    but give such a slash no meaning unless the pathname is just that slash. 

    This function assumes its argument is a non-null string and it returns 
    a non-null string. The returned string is empty if and only if the only 
    slash in the pathname is at the end.  */

function PathnameGetPath (Pathname)
{
    if (Pathname == "/") return "/";

    var pathname = Pathname.charAt (Pathname.length - 1) == "/"
                    ? Pathname.substr (0, Pathname.length - 1) 
                    : Pathname;

    var i = pathname.lastIndexOf ("/");
    if (i == -1) return "";
    if (i == 0) return "/";
    return pathname.substr (0, i);
}

/*  Given a pathname (to a file or directory) and a path (to a directory), 
    return a pathname that is relative to the path. Allow that the path may 
    end with a slash, but give such a slash no meaning. 

    This function assumes its arguments are non-null strings. The returned 
    string is null if and only if the given pathname is not on the given 
    path. The returned string is empty if and only if the given pathname is 
    the given path.  */

function PathnameGetRelative (Pathname, Path)
{
    var path = Path.charAt (Path.length - 1) == "/"
                ? Path.substr (0, Path.length - 1)
                : Path;

    if (Pathname.substr (0, path.length) == path) {

        if (Pathname.length == path.length) return "";

        if (Pathname.charAt (path.length) == "/") {
            return Pathname.substr (path.length + 1);
        }
    }
    return null;
}

/*  ********  */
/*  Location  */

/*  Provide for assembling local URLs from an absolute pathname, bookmark 
    and search string, and for composing a search string from parsed 
    arguments. The bookmark and search string are optional at construction.  */

function LocalUrl (Pathname, Hash, Search)
{
    /*  An absolute pathname taken from the pathname member of a link will 
        typically (and apparently deliberately) not have a leading slash.  */
    
    if (Pathname.charAt (0) != "/") Pathname = "/" + Pathname;
    this.pathname = Pathname;

    /*  It seems that a hash from either a location object or link will 
        always start with a "#", if non-empty - but check, inserting one if 
        absent.  */

    if (arguments.length < 2 || Hash == null) Hash = "";
    if (Hash != "" && Hash.charAt (0) != "#") Hash = "#" + Hash;
    this.hash = Hash;

    /*  It seems that a search string from either a location object or link 
        will always start with a "?", if non-empty - but check, inserting 
        one if absent.  */

    if (arguments.length < 3 || Search == null) Search = "";
    if (Search != "" && Search.charAt (0) != "?") Search = "?" + Search;
    this.search = Search;

    this.args = new ParsedSearch ();
}

LocalUrl.prototype.AddArgument = function (Name, Value)
{
    this.args.AddArgument (Name, Value);
}

LocalUrl.prototype.AddString = function (Str)
{
    this.args.AddString (Str);
}

LocalUrl.prototype.AddParsed = function (Parsed)
{
    this.args.AddParsed (Parsed);
}

LocalUrl.prototype.toString = function ()
{
    var location = window.location;

    var href = location.protocol + "//" + location.hostname + this.pathname;

    if (this.hash != "#") href += this.hash;

    if (this.args.Count != 0) {
        this.args.AddString (this.search);
        return href + this.args;
    }
    else {
        if (this.search != "?") href += this.search;
        return href;
    }
}

/*  ================================  */
/*  Some very rudimentary derivation  */

/*  For constructing a LocalUrl from a location object  */

function LocalLocation (Location)
{
    if (LocalUrl.call != null) {
        LocalUrl.call (this, Location.pathname, Location.hash, Location.search);
    }
    else {
        return new LocalUrl (Location.pathname, Location.hash, Location.search);
    }
}

LocalLocation.prototype = LocalUrl.prototype;

/*  For constructing a LocalUrl from a link element  */

function LocalLink (Link)
{
    var pathname = Link.pathname;
    if (pathname.charAt (0) != "/") pathname = "/" + pathname;

    if (LocalUrl.call != null) {
        LocalUrl.call (this, pathname, Link.hash, Link.search);
    }
    else {
        return new LocalUrl (pathname, Link.hash, Link.search);
    }
}

LocalLink.prototype = LocalUrl.prototype;

/*  *******  */
/*  Viewers  */

/*  The following function determines whether the current window is the 
    expected frame.  */

function IsViewerFrame (FrameName)
{
    /*  If the current window does not itself have the expected name, then 
        it is certainly not the expected frame.  */

    if (window.name != FrameName) return false;

    /*  If the current window is the top window, then it can't be any 
        frame.  */

    var top = window.top;
    if (window == top) return false;

    /*  If the top window is not our viewer, then although the current 
        window may be a frame, even with the expected name, it is not our 
        frame.  */

    if (top.name != sViewerWindow) return false;

    /*  Is the current window known to the top window?  */

    if (window != top [FrameName]) return false;

    /*  Perhaps getting over-cautious, check that the current window 
        actually is among the frames of the top window. 

        We might think to have MSHTML pick the named frames from the top 
        window, e.g., with something like 

            top.frames (FrameName, 0)

        but this returns an error, not null, if no such frame exists!  */

    var frames = top.frames;
    var numframes = frames.length;
    for (var n = 0; n < numframes; n ++) {
        if (frames [n] == window) return true;
    }
    return false;
}

var bIsRecognisedHostName = false;
var bIsHostNameTested = false;

function IsRecognisedHostName ()
{
    if (!bIsHostNameTested) {
        var hostname = window.location.hostname;
        var count = aRecognisedHostNames.length;
        for (var i = 0; i < count; i ++) {
            var re = aRecognisedHostNames [i];
            if (re.test (hostname)) {
                bIsRecognisedHostName = true;
                break;
            }
        }
        bIsHostNameTested = true;
    }
    return bIsRecognisedHostName;
}

/*  The viewer is in either (but exceptionally) the same directory as the 
    document or (more generally) some directory higher up the directory 
    tree, possibly all the way to the root. Indeed, the present 
    implementation has one viewer for the whole site, and it is in the root. 

    A document page is communicated to its viewer via a search-string 
    argument. For example, a document page with the URL 

        http://hostname/[viewerpath/][relpath/]document.htm 

    is re-presented via its viewer, using the URL 

        http://hostname/[viewerpath/]viewer.htm?doc=[relpath/]document.htm 

    where the path from the root to the document has been separated into a 
    path from the root to the viewer and a relative path from the viewer to 
    the document.  */

LocalUrl.prototype.View = function ()
{
    var pathname = this.pathname;
    var viewerpath = null;
    var relpath = null;
    for (var n = 0; n < asViewerPaths.length; n ++) {
        viewerpath = asViewerPaths [n];
        relpath = PathnameGetRelative (pathname, viewerpath);
        if (relpath != null) break;
    }
    if (relpath == null) {
        viewerpath = "/";
        relpath = pathname.charAt (0) == "/" ? pathname.substr (1) : pathname;
    }
    if (relpath == "") relpath = sDefaultFilename;

    this.pathname = PathAppend (viewerpath, sViewerFilename);

    this.AddArgument (sDocumentArgumentName, relpath);

    var hash = this.hash;
    if (hash.charAt (0) == "#") hash = hash.substr (1);
    if (hash != "") this.AddArgument (sBookmarkArgumentName, hash);
    this.hash = "";

    var viewer = window.top;
    if (viewer.name == sViewerWindow) {
        var toc = viewer [sTocFrame];
        if (toc != null) {
            var args = toc.ComposeTocArguments (new LocalUrl (pathname));
            if (args != null) this.AddParsed (args);
        }
    }

    return this.toString ();
}

function ViewDefaultFileInSameDirectory ()
{
    var path = PathnameGetPath (window.location.pathname);
    var pathname = PathAppend (path, sDefaultFilename);
    window.top.location.replace (new LocalUrl (pathname).View ());
}

/*  ==============  */
/*  Internal Links  */

/*  Links within the site must be redirected through the viewer, to avoid 
    confusing the Forward and Back navigation. 

    Call the RedirectLocalLink function only after establishing that the 
    link is local.  */

function RedirectLocalLink (Link)
{
    if (Link.pathname.indexOf ("/_") == -1) {
        Link.target = "_top";
        Link.href = new LocalLink (Link).View ();
    }
}

/*  Most often best is to redirect a local link only when someone actually 
    does try to follow it.  */

function RedirectClickedLink (Event)            // onclick 
{
    if (Event == null) Event = window.event;
    for (var x = GetEventSource (Event); x != null; x = x.parentNode) {
        if (x.tagName == "A") {
            if (x.hostname == window.location.hostname) RedirectLocalLink (x);
            return;
        }
    }
}

/*  *******  */
/*  Subwebs  */

function GetSubwebPath (Pathname)
{
    for (var n = 0; n < asSubwebPaths.length; n ++) {
        var subweb = asSubwebPaths [n];
        if (PathnameGetRelative (Pathname, subweb) != null) return subweb;
    }
    return null;
}

/*  *****************************************  */
/*  Hiding Warnings About Scripts Not Running  */

function HideNoScriptBlocks ()
{
    var sheet = GetLastStyleSheet ();
    if (sheet == null) return false;

    return AppendStyleSheetRule (sheet, "." + sNoScriptClass, "display:none;");
}

/*  ********************  */
/*  Block a Bad Referrer  */

function BadReferrer (Source, Target)
{
    this.Source = Source;
    this.Target = Target;
}

var aBadReferrers = new Array (
    new BadReferrer (/(.+\.)*msfn(\..+)*/i, "/redirect/msfn.htm")
);

function IsBadReferrer ()
{
    var ref = window.document.referrer;
    if (ref == null) return null;

    var count = aBadReferrers.length;
    for (var n = 0; n < count; n ++) {
        var bad = aBadReferrers [n];
        if (bad.Source.test (ref)) return bad.Target;
    }
    return null;
}

function RedirectBadReferrer ()
{
    var target = IsBadReferrer ();
    if (target != null) window.top.location.pathname = target;
}

RedirectBadReferrer ();

/*  Copyright © 2007-2009. Geoff Chappell. All rights reserved.  */

//-->