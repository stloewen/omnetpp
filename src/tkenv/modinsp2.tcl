#==========================================================================
#  MODINSP.TCL -
#            part of the Tcl/Tk windowing environment of
#                            OMNeT++
#==========================================================================

#----------------------------------------------------------------#
#  Copyright (C) 1992-2003 Andras Varga
#
#  This file is distributed WITHOUT ANY WARRANTY. See the file
#  `license' for details on this and other legal matters.
#----------------------------------------------------------------#

#-----------------------------------------------------------------
#  Graphical compound module window stuff
#-----------------------------------------------------------------


# split_dispstr --
#
# Split up display string into an array.
#    str:     display string
#    array:   dest array name
#    w:       inspector window name
#    modptr:  pointer of module whose parameters should be used for "$x" style
#             parameters in the display string
#    parent:  if nonzero, parameter is searched in the parent module too;
#             otherwise, only that very module is considered
# Example:
#   if "p=50,$y_pos;i=cloud" is parsed into array 'a' and the "y_pos" module
#   parameter is 99, the result is:
#      $a(p) = {50 99}
#      $a(i) = {cloud}
#
proc split_dispstr {str array w modptr parent} {
   upvar $array arr

   foreach tag [split $str {;}] {
      set tag [split $tag {=}]
      set key [lindex $tag 0]
      set val [split [lindex $tag 1] {,}]

      set i 0
      foreach v $val {
         if {[string range $v 0 0]=={$}} {
            if {$modptr==""} {error "Cannot substitute parameters into this display string"}
            set v [opp_inspectorcommand $w dispstrpar $modptr [string range $v 1 end] $parent]
            set val [lreplace $val $i $i $v]
         }
         incr i
      }

      if {$key != ""} {
         set arr($key) $val
      }
   }
}


#
# helper function
#
proc dispstr_getimage {tags_i} {
    global icons imagecache

    set img [lindex $tags_i 0]
    if {$img=="" || [catch {image type $img}]} {
        set img $icons(unknown)
    }

    if {[llength $tags_i]>1} {
        # check destcolor, weight for icon colorizing
        set destc [lindex $tags_i 1]
        set cweight [lindex $tags_i 2]
        if {$destc==""} {
            # without destcolor, weight is ignored
            return $img
        } elseif {[string index $destc 0]== "@"} {
            set destc [opp_hsb_to_rgb $destc]
        }
        if {$cweight==""} {
            set cweight 30
        }

        # look up or create a new image with the given parameters
        if [info exist imagecache($img,$destc,$cweight)] {
            set img $imagecache($img,$destc,$cweight)
        } else {
            set img2 [image create photo]
            $img2 copy $img
            opp_colorizeimage $img2 $destc $cweight
            set imagecache($img,$destc,$cweight) $img2
            set img $img2
        }
    }
    return $img
}

#
# helper function
#
proc get_submod_coords {c tag} {

   set id [$c find withtag $tag]
   if {$id==""} {error "$tag not found"}
   return [$c bbox $tag]  ;#-- this could be faster, but somehow doesn't work properly with senddirect animation

   if {[$c type $id]=="image"} {
       set pos [$c coords $id]
       set x [lindex $pos 0]
       set y [lindex $pos 1]
       set img [$c itemcget $id -image]
       set hwidth  [expr [image width $img] / 2]
       set hheight [expr [image height $img] / 2]

       set coords "[expr $x-$hwidth] [expr $y-$hheight] \
                       [expr $x+$hwidth] [expr $y+$hheight]"
   } else {
       set coords [$c coords $id]
   }
   return $coords
}


# draw_submod --
#
# This function is invoked from the module inspector C++ code.
#
proc draw_submod {c submodptr x y name dispstr} {
   #puts "DEBUG: draw_submod $c $submodptr $x $y $name $dispstr"

   if [catch {

       split_dispstr $dispstr tags [winfo toplevel $c] $submodptr 1

       # set sx and sy (and look up image)
       set isx 0
       set isy 0
       set bsx 0
       set bsy 0
       if [info exists tags(i)] {
           set img [dispstr_getimage $tags(i)]
           set isx [image width $img]
           set isy [image height $img]
       }
       if [info exists tags(b)] {
           set bsx [lindex $tags(b) 0]
           if {$bsx==""} {set bsx 40}
           set bsy [lindex $tags(b) 1]
           if {$bsy==""} {set bsy [expr 0.6*$bsx]}
       } elseif ![info exists tags(i)] {
           set tags(b) {40 24 rect}
           set bsx 40
           set bsy 24
       }
       set sx [expr {$isx<$bsx ? $bsx : $isx}]
       set sy [expr {$isy<$bsy ? $bsy : $isy}]

       if [info exists tags(b)] {

           set x1 [expr $x - $bsx/2]
           set y1 [expr $y - $bsy/2]
           set x2 [expr $x + $bsx/2]
           set y2 [expr $y + $bsy/2]

           set sh [lindex $tags(b) 2]
           if {$sh == ""} {set sh rect}

           if {![info exists tags(o)]} {set tags(o) {}}
           set fill [lindex $tags(o) 0]
           if {$fill == ""} {set fill #8080ff}
           if {$fill == "-"} {set fill ""}
           if {[string index $fill 0]== "@"} {set fill [opp_hsb_to_rgb $fill]}
           set outline [lindex $tags(o) 1]
           if {$outline == ""} {set outline black}
           if {$outline == "-"} {set outline ""}
           if {[string index $outline 0]== "@"} {set outline [opp_hsb_to_rgb $outline]}
           set width [lindex $tags(o) 2]
           if {$width == ""} {set width 2}

           $c create $sh $x1 $y1 $x2 $y2 \
               -fill $fill -width $width -outline $outline \
               -tags "dx tooltip submod $submodptr"

           if [info exists tags(i)] {
               $c create image $x $y -image $img -anchor center -tags "dx tooltip submod $submodptr"
           }

           $c create text $x [expr $y2+$width/2+3] -text $name -anchor n -tags "dx"

       } elseif [info exists tags(i)] {

           $c create image $x $y -image $img -anchor center -tags "dx tooltip submod $submodptr"
           $c create text $x [expr $y+$sy/2+3] -text $name -anchor n -tags "dx"

       }

       # queue length
       if {[info exists tags(q)]} {
           set r [get_submod_coords $c $submodptr]
           set x [expr [lindex $r 2]+1]
           set y [lindex $r 1]
           $c create text $x $y -text "q:?" -anchor nw -tags "dx qlen qlen-$submodptr"
       }

       # r=<radius>,<fillcolor>,<color>,<width>
       if {[info exists tags(r)]} {
           set radius [lindex $tags(r) 0]
           if {$radius == ""} {set radius 100}
           set rfill [lindex $tags(r) 1]
           if {$rfill == "-"} {set rfill ""}
           if {[string index $rfill 0]== "@"} {set rfill [opp_hsb_to_rgb $rfill]}
           # if rfill=="" --> not filled
           set routline [lindex $tags(r) 2]
           if {$routline == "" && $rfill == ""} {set routline black}
           if {$routline == "-"} {set routline ""}
           if {[string index $routline 0]== "@"} {set routline [opp_hsb_to_rgb $routline]}
           set rwidth [lindex $tags(r) 3]
           if {$rwidth == ""} {set rwidth 1}

           set x1 [expr $x - $radius/2]
           set y1 [expr $y - $radius/2]
           set x2 [expr $x + $radius/2]
           set y2 [expr $y + $radius/2]

           set circle [$c create oval $x1 $y1 $x2 $y2 \
               -fill $rfill -width $rwidth -outline $routline -tags "dx"]
           $c lower $circle
       }

   } errmsg] {
       tk_messageBox -type ok -title Error -icon error \
                     -message "Error in display string of $name: $errmsg"
   }
}


# draw_enclosingmod --
#
# This function is invoked from the module inspector C++ code.
#
proc draw_enclosingmod {c ptr name dispstr} {

   # puts "DEBUG: draw_enclosingmod $c $ptr $name $dispstr"

   if [catch {

       split_dispstr $dispstr tags [winfo toplevel $c] $ptr 0

       # determine top-left origin
       if {![info exists tags(p)]} {set tags(p) {}}
       set bx [lindex $tags(p) 0]
       set by [lindex $tags(p) 1]
       if {$bx==""} {set bx 10}
       if {$by==""} {set by 10}

       if {![info exists tags(b)]} {set tags(b) {{} {} {}}}

       set sx [lindex $tags(b) 0]
       set sy [lindex $tags(b) 1]
       if {$sx=="" || $sy==""} {
           set bb [$c bbox submod]
           if {$bb==""} {set bb "$bx $by 300 200"}
           if {[lindex $bb 0]<$bx} {set bx [expr [lindex $bb 0]-10]}
           if {[lindex $bb 1]<$by} {set by [expr [lindex $bb 1]-10]}
           if {$sx==""} {set sx [expr [lindex $bb 2]+[lindex $bb 0]-2*$bx]}
           if {$sy==""} {set sy [expr [lindex $bb 3]+[lindex $bb 1]-2*$by]}
       }

       set sh [lindex $tags(b) 2]
       if {$sh == ""} {set sh rect}

       if {![info exists tags(o)]} {set tags(o) {}}
       set fill [lindex $tags(o) 0]
       if {$fill == ""} {set fill #c0c0c0}
       if {$fill == "-"} {set fill ""}
       if {[string index $fill 0]== "@"} {set fill [opp_hsb_to_rgb $fill]}
       set outline [lindex $tags(o) 1]
       if {$outline == ""} {set outline black}
       if {$outline == "-"} {set outline ""}
       if {[string index $outline 0]== "@"} {set outline [opp_hsb_to_rgb $outline]}
       set width [lindex $tags(o) 2]
       if {$width == ""} {set width 2}

       $c create $sh $bx $by [expr $bx+$sx] [expr $by+$sy] \
           -fill $fill -width $width -outline $outline \
           -tags "dx mod $ptr"
       $c create text [expr $bx+3] [expr $by+3] -text $name -anchor nw -tags "dx tooltip modname"
       $c lower mod

       set bb [$c bbox all]
       $c config -scrollregion [list [expr [lindex $bb 0]-10] [expr [lindex $bb 1]-10] \
                                     [expr [lindex $bb 2]+10] [expr [lindex $bb 3]+10]]


   } errmsg] {
       tk_messageBox -type ok -title Error -icon error \
                     -message "Error in display string of $name: $errmsg"
   }
}


# draw_connection --
#
# This function is invoked from the module inspector C++ code.
#
proc draw_connection {c gateptr dispstr srcptr destptr src_i src_n dest_i dest_n} {

    # puts "DEBUG: draw_connection $c $gateptr $dispstr $srcptr $destptr $src_i $src_n $dest_i $dest_n"

    if [catch {
       set src_rect [get_submod_coords $c $srcptr]
       set dest_rect [get_submod_coords $c $destptr]
    } errmsg] {
       # skip this connection if source or destination of the arrow cannot be found
       return
    }

    if [catch {

       split_dispstr $dispstr tags [winfo toplevel $c] {} 0

       if {![info exists tags(m)]} {set tags(m) {a}}

       set mode [lindex $tags(m) 0]
       if {$mode==""} {set mode "a"}
       set src_anch  [list [lindex $tags(m) 1] [lindex $tags(m) 2]]
       set dest_anch [list [lindex $tags(m) 3] [lindex $tags(m) 4]]

       # puts "DEBUG: src_rect=($src_rect) dest_rect=($dest_rect)"
       # puts "DEBUG: src_anch=($src_anch) dest_anch=($dest_anch)"

       regexp -- {^.[^.]*} $c win

       set arrow_coords [eval [concat opp_inspectorcommand $win arrowcoords \
                  $src_rect $dest_rect $src_i $src_n $dest_i $dest_n \
                  $mode $src_anch $dest_anch]]

       # puts "DEBUG: arrow=($arrow_coords)"

       if {![info exists tags(o)]} {set tags(o) {}}
       set fill [lindex $tags(o) 0]
       if {$fill == ""} {set fill black}
       set width [lindex $tags(o) 1]
       if {$width == ""} {set width 1}

       $c create line $arrow_coords -arrow last -fill $fill -width $width -tags "dx tooltip conn $gateptr"

    } errmsg] {
       tk_messageBox -type ok -title Error -icon error \
                     -message "Error in display string of a connection: $errmsg"
    }
}


# draw_message --
#
# This function is invoked from the message animation code.
#
proc draw_message {c msgptr x y msgname msgkind} {
    global fonts
    set dispstr [opp_getobjectfield $msgptr displayString]

    if {$dispstr=="" && [opp_getsimoption penguin_mode]} {
        # following lines were used for testing only...
        #set dispstr "b=15,15,rect;o=white,kind,5"
        #set dispstr "b="
        #set dispstr "o=kind"
        #set dispstr "b=15,15,oval;o=kind,white,6"
        #set dispstr "i=handset2_s"
        set dispstr "i=penguin"
    }

    if {$dispstr==""} {

        # default presentation: red or msgkind%8-colored ball
        if [opp_getsimoption animation_msgcolors] {
            set color [lindex {red green blue white yellow cyan magenta black} [expr $msgkind % 8]]
        } else {
            set color red
        }
        set ball [$c create oval -5 -5 5 5 -fill $color -outline $color -tags "dx tooltip msg $msgptr"]
        $c move $ball $x $y

        if [opp_getsimoption animation_msgnames] {
            $c create text $x $y -text $msgname -anchor n -font $fonts(msgname) -tags "dx tooltip msgname $msgptr"
        }

    } else {
        # use display string

        # supports "b","i" and "o" tags, they work just as with submodules only default
        # is different (small red ball), plus special color "kind" is supported which
        # gives the original, message kind dependent colors
        split_dispstr $dispstr tags [winfo toplevel $c] {} 1

        # set sx and sy
        if [info exists tags(i)] {
            set img [dispstr_getimage $tags(i)]
            set sx [image width $img]
            set sy [image height $img]
        } elseif [info exists tags(b)] {
            set sx [lindex $tags(b) 0]
            if {$sx==""} {set sx 10}
            set sy [lindex $tags(b) 1]
            if {$sy==""} {set sy $sx}
        } else {
            set tags(b) {10 10 oval}
            set sx 10
            set sy 10
        }

        if [info exists tags(i)] {

            $c create image $x $y -image $img -anchor center -tags "dx tooltip msg $msgptr"
            if [opp_getsimoption animation_msgnames] {
                $c create text $x [expr $y+$sy/2+3] -text $msgname -anchor n -tags "dx tooltip msgname $msgptr"
            }

        } elseif [info exists tags(b)] {

            set x1 [expr $x - $sx/2]
            set y1 [expr $y - $sy/2]
            set x2 [expr $x + $sx/2]
            set y2 [expr $y + $sy/2]

            set sh [lindex $tags(b) 2]
            if {$sh == ""} {set sh oval}

            if {![info exists tags(o)]} {set tags(o) {}}
            set fill [lindex $tags(o) 0]
            if {$fill == ""} {set fill red}
            if {$fill == "kind"} {
                set fill [lindex {red green blue white yellow cyan magenta black} [expr $msgkind % 8]]
            }
            set outline [lindex $tags(o) 1]
            if {$outline == ""} {set outline ""}
            if {$outline == "kind"} {
                set outline [lindex {red green blue white yellow cyan magenta black} [expr $msgkind % 8]]
            }
            set width [lindex $tags(o) 2]
            if {$width == ""} {set width 1}

            $c create $sh $x1 $y1 $x2 $y2 -fill $fill -width $width -outline $outline -tags "dx tooltip msg $msgptr"
            if [opp_getsimoption animation_msgnames] {
                $c create text $x [expr $y2+$width/2+3] -text $msgname -anchor n -tags "dx tooltip msgname $msgptr"
            }
        }
    }
}

proc create_graphicalmodwindow {name geom} {
    global icons help_tips

    set w $name
    create_inspector_toplevel $w $geom

    # create toolbar
    iconbutton $w.toolbar.ascont -image $icons(asobject) -command "inspect_this $w {As Object}"
    iconbutton $w.toolbar.win    -image $icons(asoutput) -command "inspect_this $w {Module output}"
    iconbutton $w.toolbar.sep1   -separator
    iconbutton $w.toolbar.parent -image $icons(parent) ;#command assigned from C++
    iconbutton $w.toolbar.sep2   -separator
    iconbutton $w.toolbar.mrun   -image $icons(mrun) -command "module_run $w"
    iconbutton $w.toolbar.mfast  -image $icons(mfast) -command "module_run_fast $w"
    iconbutton $w.toolbar.stop   -image $icons(stop) -command "stop_simulation"
    iconbutton $w.toolbar.sep3   -separator
    iconbutton $w.toolbar.redraw -image $icons(redraw) -command "opp_inspectorcommand $w relayout"
    foreach i {ascont win sep1 parent sep2 mrun mfast stop sep3 redraw} {
       pack $w.toolbar.$i -anchor n -side left -padx 0 -pady 2
    }
    bind $w <Control-F4> "module_run_fast $w"

    set help_tips($w.toolbar.ascont)  {Inspect as object}
    set help_tips($w.toolbar.win)     {See module output}
    set help_tips($w.toolbar.parent)  {Inspect parent module}
    set help_tips($w.toolbar.mrun)    {Run until next local event}
    set help_tips($w.toolbar.mfast)   {Fast run until next local event (Ctrl-F4)}
    set help_tips($w.toolbar.stop)    {Stop running simulation (F8)}
    set help_tips($w.toolbar.redraw)  {Re-layout}

    # create canvas
    set c $w.c

    frame $w.grid
    scrollbar $w.hsb -orient horiz -command "$c xview"
    scrollbar $w.vsb -command "$c yview"
    canvas $c -background #a0e0a0 -relief raised \
        -xscrollcommand "$w.hsb set" \
        -yscrollcommand "$w.vsb set"
    pack $w.grid -expand yes -fill both -padx 1 -pady 1
    grid rowconfig    $w.grid 0 -weight 1 -minsize 0
    grid columnconfig $w.grid 0 -weight 1 -minsize 0

    grid $c -in $w.grid -row 0 -column 0 -rowspan 1 -columnspan 1 -sticky news
    grid $w.vsb -in $w.grid -row 0 -column 1 -rowspan 1 -columnspan 1 -sticky news
    grid $w.hsb -in $w.grid -row 1 -column 0 -rowspan 1 -columnspan 1 -sticky news

    # mouse bindings
    $c bind submod <Double-1> "graphmodwin_dblclick $c"
    $c bind conn <Double-1> "graphmodwin_dblclick $c"
    $c bind msg <Double-1> "graphmodwin_dblclick $c"
    $c bind msgname <Double-1> "graphmodwin_dblclick $c"
    $c bind qlen <Double-1> "graphmodwin_qlen_dblclick $c"

    $c bind submod <3> "graphmodwin_rightclick $c %X %Y"
    $c bind conn <3> "graphmodwin_rightclick $c %X %Y"
    $c bind msg <3> "graphmodwin_rightclick $c %X %Y"
    $c bind msgname <3> "graphmodwin_rightclick $c %X %Y"
    $c bind mod <3> "graphmodwin_rightclick $c %X %Y"
    $c bind modname <3> "graphmodwin_rightclick $c %X %Y"
    $c bind qlen <3> "graphmodwin_qlen_rightclick $c %X %Y"

    #update idletasks
    update
    opp_inspectorcommand $w relayout
}

proc graphmodwin_dblclick c {

   set item [$c find withtag current]
   set tags [$c gettags $item]

   set ptr ""
   if {[lsearch $tags "ptr*"] != -1} {
      regexp "ptr.*" $tags ptr
   }

   if {$ptr!=""} {
      opp_inspect $ptr "(default)"
   }
}

proc graphmodwin_rightclick {c X Y} {
   set item [$c find withtag current]
   set tags [$c gettags $item]

   set ptr ""
   if {[lsearch $tags "ptr*"] != -1} {
      regexp "ptr.*" $tags ptr
   }
   set ptr [lindex $ptr 0]

   if {$ptr!=""} {
      popup_insp_menu $ptr $X $Y
   }
}


# graphmodwin_draw_message_on_gate --
#
# Draw message near the head of the connection arrow.
# Called from inspector C++ code.
#
proc graphmodwin_draw_message_on_gate {c gateptr msgptr msgname msgkind} {

    #puts "DBG: graphmodwin_draw_message_on_gate $msgptr"

    global fonts

    # gate pointer + conn are the tags of the connection arrow
    set conn_id ""
    foreach id [$c find withtag $gateptr] {
       if {[lsearch -exact [$c gettags $id] "conn"] != -1} {
          set conn_id $id
       }
    }

    if {$conn_id == ""} {
        # connection arrow not (no longer?) on canvas: forget animation
        $c delete $msgptr;  # this also works if msg is not (yet) on canvas
        return;
    }

    set coords [$c coords $conn_id]

    set x1 [lindex $coords 0]
    set y1 [lindex $coords 1]
    set x2 [lindex $coords 2]
    set y2 [lindex $coords 3]
    set len [expr sqrt(($x2-$x1)*($x2-$x1)+($y2-$y1)*($y2-$y1))]

    set steps [expr int($len/2)]
    if {$steps>100} {set steps 100}
    if {$steps==0} {set steps 1}

    set dx [expr ($x2-$x1)/$steps]
    set dy [expr ($y2-$y1)/$steps]

    set steps [expr $steps<6 ? 0 : $steps-6]
    set xx [expr $x1+$dx*$steps]
    set yy [expr $y1+$dy*$steps]

    draw_message $c $msgptr $xx $yy $msgname $msgkind
}

# graphmodwin_draw_message_on_module --
#
# Draw message on submodule rectangle.
# Called from inspector C++ code.
#
proc graphmodwin_draw_message_on_module {c modptr msgptr msgname msgkind} {

    #puts "DBG: graphmodwin_draw_message_on_module $msgptr"
    set r  [get_submod_coords $c $modptr]
    set x [expr ([lindex $r 0]+[lindex $r 2])/2]
    set y [expr ([lindex $r 1]+[lindex $r 3])/2]

    draw_message $c $msgptr $x $y $msgname $msgkind
}


#
# Called from C++ code. $mode="beg"/"thru"/"end".
#
proc graphmodwin_animate_on_conn {win gateptr msgptr msgname msgkind mode} {

    #puts "DBG: send $mode $msgptr"

    set c $win.c

    # gate pointer string is the tag of the connection arrow
    set coords [$c coords $gateptr]

    if {$coords == ""} {
        # connection arrow not (no longer?) on canvas: forget animation
        $c delete $msgptr;  # this also works if msg is not (yet) on canvas
        return;
    }

    set x1 [lindex $coords 0]
    set y1 [lindex $coords 1]
    set x2 [lindex $coords 2]
    set y2 [lindex $coords 3]

    graphmodwin_do_animate $win $x1 $y1 $x2 $y2 $msgptr $msgname $msgkind $mode

    if {$mode!="beg"} {
       $c delete $msgptr
    }
}


#
# Called from C++ code. $mode="beg"/"thru"/"end".
#
proc graphmodwin_animate_senddirect_horiz {win mod1ptr mod2ptr msgptr msgname msgkind mode} {

    #puts "DBG: senddirect horiz $mode $msgptr"

    set c $win.c
    set src  [get_submod_coords $c $mod1ptr]
    set dest [get_submod_coords $c $mod2ptr]

    set x1 [expr ([lindex $src 0]+[lindex $src 2])/2]
    set y1 [expr ([lindex $src 1]+[lindex $src 3])/2]
    set x2 [expr ([lindex $dest 0]+[lindex $dest 2])/2]
    set y2 [expr ([lindex $dest 1]+[lindex $dest 3])/2]

    graphmodwin_do_animate_senddirect $win $x1 $y1 $x2 $y2 $msgptr $msgname $msgkind $mode
}


#
# Called from C++ code. $mode="beg"/"thru"/"end".
#
proc graphmodwin_animate_senddirect_ascent {win parentmodptr modptr msgptr msgname msgkind mode} {

    #puts "DBG: senddirect ascent $mode $msgptr"

    set c $win.c
    set src  [get_submod_coords $c $modptr]

    set x1 [expr ([lindex $src 0]+[lindex $src 2])/2]
    set y1 [expr ([lindex $src 1]+[lindex $src 3])/2]
    set x2 [expr $x1 + $y1/4]
    set y2 0

    graphmodwin_do_animate_senddirect $win $x1 $y1 $x2 $y2 $msgptr $msgname $msgkind $mode
}


#
# Called from C++ code. $mode="beg"/"thru"/"end".
#
proc graphmodwin_animate_senddirect_descent {win parentmodptr modptr msgptr msgname msgkind mode} {

    #puts "DBG: senddirect descent $mode $msgptr"

    set c $win.c
    set dest [get_submod_coords $c $modptr]

    set x2 [expr ([lindex $dest 0]+[lindex $dest 2])/2]
    set y2 [expr ([lindex $dest 1]+[lindex $dest 3])/2]
    set x1 [expr $x2 - $y2/4]
    set y1 0

    graphmodwin_do_animate_senddirect $win $x1 $y1 $x2 $y2 $msgptr $msgname $msgkind $mode
}


#
# Called from C++ code. $mode="beg"/"thru"/"end".
#
proc graphmodwin_animate_senddirect_delivery {win modptr msgptr msgname msgkind} {

    #puts "DBG: senddirect deliv $msgptr"

    global animdelay
    set c $win.c
    set src  [get_submod_coords $c $modptr]

    set sp [opp_getsimoption animation_speed]
    set ad [expr $animdelay / (0.1+$sp*$sp)]

    # flash the message a few times before removing it
    for {set i 0} {$i<3} {incr i} {
       $c itemconfig $msgptr -state hidden
       update idletasks
       for {set j 0} {$j<3*$ad} {incr j} {}
       $c itemconfig $msgptr -state normal
       update idletasks
       for {set j 0} {$j<3*$ad} {incr j} {}
    }

    $c delete $msgptr
}


#
# Helper for senddirect animations
#
proc graphmodwin_do_animate_senddirect {win x1 y1 x2 y2 msgptr msgname msgkind mode} {
    set c $win.c

    if [opp_getsimoption senddirect_arrows] {
        #$c create line $x1 $y1 $x2 $y2 -tags {senddirect} -arrow last -fill gray
        $c create line $x1 $y1 $x2 $y2 -tags {senddirect} -arrow last -fill blue -dash {.}
        graphmodwin_do_animate $win $x1 $y1 $x2 $y2 $msgptr $msgname $msgkind "thru"
        #$c delete $arrow -- this will come in _cleanup
    } else {
        graphmodwin_do_animate $win $x1 $y1 $x2 $y2 $msgptr $msgname $msgkind "thru"
    }
    if {$mode!="beg"} {
       $c delete $msgptr
    }
}


#
# Ultimate helper function which in fact performs the animation.
#
proc graphmodwin_do_animate {win x1 y1 x2 y2 msgptr msgname msgkind {mode thru}} {

    global animdelay fonts
    set c $win.c

    # remove "phantom messages" if any
    $c delete $msgptr

    # msg will travel at constant speed: $steps proportional to length
    set len [expr sqrt(($x2-$x1)*($x2-$x1)+($y2-$y1)*($y2-$y1))]
    set steps [expr int($len/2)]

    # max 100 steps (otherwise animation takes forever!)
    if {$steps>100} {set steps 100}
    if {$steps==0} {set steps 1}

    set dx [expr ($x2-$x1)/double($steps)]
    set dy [expr ($y2-$y1)/double($steps)]

    switch $mode {
       beg -
       thru {
          draw_message $c $msgptr $x1 $y1 $msgname $msgkind
       }
       end {}
    }
    switch $mode {
       beg {set steps [expr $steps<6 ? 0 : $steps-6]}
       end {set steps 6}
    }

    set sp [opp_getsimoption animation_speed]
    set ad [expr $animdelay / (0.1+$sp*$sp)]

    for {set i 0} {$i<$steps} {incr i} {
       update idletasks
       $c move $msgptr $dx $dy
       for {set j 0} {$j<$ad} {incr j} {}
    }
}

#
# This function is invoked from the module inspector C++ code.
#
proc graphmodwin_animate_senddirect_cleanup {win} {
    set c $win.c
    $c delete senddirect
}


# graphmodwin_animate_methodcall_ascent --
#
# This function is invoked from the module inspector C++ code.
#
proc graphmodwin_animate_methodcall_ascent {win parentmodptr modptr methodlabel} {
    set c $win.c
    set src  [get_submod_coords $c $modptr]

    set x1 [expr ([lindex $src 0]+[lindex $src 2])/2]
    set y1 [expr ([lindex $src 1]+[lindex $src 3])/2]
    set x2 [expr $x1 + $y1/4]
    set y2 0
    graphmodwin_do_draw_methodcall $win $x1 $y1 $x2 $y2 $methodlabel
}

# graphmodwin_animate_methodcall_descent --
#
# This function is invoked from the module inspector C++ code.
#
proc graphmodwin_animate_methodcall_descent {win parentmodptr modptr methodlabel} {
    set c $win.c
    set dest [get_submod_coords $c $modptr]

    set x2 [expr ([lindex $dest 0]+[lindex $dest 2])/2]
    set y2 [expr ([lindex $dest 1]+[lindex $dest 3])/2]
    set x1 [expr $x2 - $y2/4]
    set y1 0
    graphmodwin_do_draw_methodcall $win $x1 $y1 $x2 $y2 $methodlabel
}

# graphmodwin_animate_methodcall_horiz --
#
# This function is invoked from the module inspector C++ code.
#
proc graphmodwin_animate_methodcall_horiz {win fromptr toptr methodlabel} {
    set c $win.c
    set src  [get_submod_coords $c $fromptr]
    set dest [get_submod_coords $c $toptr]

    set x1 [expr ([lindex $src 0]+[lindex $src 2])/2]
    set y1 [expr ([lindex $src 1]+[lindex $src 3])/2]
    set x2 [expr ([lindex $dest 0]+[lindex $dest 2])/2]
    set y2 [expr ([lindex $dest 1]+[lindex $dest 3])/2]
    graphmodwin_do_draw_methodcall $win $x1 $y1 $x2 $y2 $methodlabel
}

# graphmodwin_do_draw_methodcall --
#
# Helper.
#
proc graphmodwin_do_draw_methodcall {win x1 y1 x2 y2 methodlabel} {
    global animdelay

    set c $win.c
    #set arrow [$c create line $x1 $y1 $x2 $y2 -tags {methodcall} -width 2 -arrow last -arrowshape {15 20 6} -fill #808080]
    set arrow [$c create line $x1 $y1 $x2 $y2 -tags {methodcall}  -dash {-} -arrow last -fill red]

    set x [expr ($x1+$x2)/2]
    set y [expr ($y1+$y2)/2]
    set txtid  [$c create text $x $y -tags {methodcall} -text " $methodlabel " -anchor c]
    set color #F0F0D0
    #catch {set color [$c itemcget mod -fill]}
    set rectid [$c create rect [$c bbox $txtid] -tags {methodcall} -outline "" -fill $color]
    $c lower $rectid $txtid

    # flash arrow a bit
    set sp [opp_getsimoption animation_speed]
    set ad [expr $animdelay / (0.1+$sp*$sp)]
    for {set i 0} {$i<2} {incr i} {
       $c itemconfig $arrow -state hidden
       update idletasks
       for {set j 0} {$j<3*$ad} {incr j} {}
       $c itemconfig $arrow -state normal
       update idletasks
       for {set j 0} {$j<3*$ad} {incr j} {}
    }
}

# graphmodwin_animate_methodcall_wait --
#
# This function is invoked from the module inspector C++ code.
#
proc graphmodwin_animate_methodcall_wait {} {
    global animdelay

    update idletasks
    set sp [opp_getsimoption methodcalls_delay]
    #set ad [expr int($animdelay / (0.1+$sp*$sp)/30)]
    after $sp
}

# graphmodwin_animate_methodcall_cleanup --
#
# This function is invoked from the module inspector C++ code.
#
proc graphmodwin_animate_methodcall_cleanup {win} {
    set c $win.c
    $c delete methodcall
}

# graphmodwin_draw_nexteventmarker --
#
# This function is invoked from the module inspector C++ code.
#
proc graphmodwin_draw_nexteventmarker {c modptr type} {
    set src  [get_submod_coords $c $modptr]
    set x1 [expr [lindex $src 0]-2]
    set y1 [expr [lindex $src 1]-2]
    set x2 [expr [lindex $src 2]+2]
    set y2 [expr [lindex $src 3]+2]
    # $type==1 compound module, $type==2 simple module
    if {$type==1} {
        #$c create rect $x1 $y1 $x2 $y2 -tags {nexteventmarker} -outline red -dash {.}
        $c create rect $x1 $y1 $x2 $y2 -tags {nexteventmarker} -outline red -width 1
    } else {
        #$c create rect $x1 $y1 $x2 $y2 -tags {nexteventmarker} -outline red
        $c create rect $x1 $y1 $x2 $y2 -tags {nexteventmarker} -outline red -width 2
    }
}

# graphmodwin_update_submod --
#
# This function is invoked from the module inspector C++ code.
#
proc graphmodwin_update_submod {c modptr} {
    # currently the only thing to be updated is the number of elements in queue
    set win [winfo toplevel $c]
    set dispstr [opp_getobjectfield $modptr displayString]
    set qname [opp_displaystring $dispstr getTagArg "q" 0]
    if {$qname!=""} {
        #set qptr [opp_inspectorcommand $win getsubmodq $modptr $qname]
        #set qlen [opp_getobjectfield $qptr length]
        # TBD optimize -- maybe store and remember q pointer?
        set qlen [opp_inspectorcommand $win getsubmodqlen $modptr $qname]
        $c itemconfig "qlen-$modptr" -text "q:$qlen"
    }
}

#
# Helper proc.
#
proc graphmodwin_qlen_getqptr {c} {
   set item [$c find withtag current]
   set tags [$c gettags $item]

   set modptr ""
   if {[lsearch $tags "qlen-ptr*"] != -1} {
       regexp "ptr.*" $tags modptr
   }
   if {$modptr==""} {return}

   set win [winfo toplevel $c]
   set dispstr [opp_getobjectfield $modptr displayString]
   set qname [opp_displaystring $dispstr getTagArg "q" 0]
   if {$qname!=""} {
       set qptr [opp_inspectorcommand $win getsubmodq $modptr $qname]
       return $qptr
   }
   return ""
}

proc graphmodwin_qlen_dblclick c {
   set qptr [graphmodwin_qlen_getqptr $c]
   if {$qptr!="" && $qptr!=[opp_object_nullpointer]} {
       opp_inspect $qptr "(default)"
   }
}

proc graphmodwin_qlen_rightclick {c X Y} {
   set qptr [graphmodwin_qlen_getqptr $c]
   if {$qptr!="" && $qptr!=[opp_object_nullpointer]} {
       popup_insp_menu $qptr $X $Y
   }
}

# graphmodwin_bubble --
#
# This function is invoked from the module inspector C++ code.
#
proc graphmodwin_bubble {c modptr txt} {
    set r  [get_submod_coords $c $modptr]
    set x [expr ([lindex $r 0]+[lindex $r 2])/2]
    set y [expr [lindex $r 1]+4]

    while {[string length $txt]<5} {set txt " $txt "}
    set txtid  [$c create text $x $y -text " $txt " -anchor c -tags "bubble"]
    set color #F8F8D8
    set bb [$c bbox $txtid]

    set x1 [lindex $bb 0]
    set y1 [lindex $bb 1]
    set x2 [lindex $bb 2]
    set y2 [lindex $bb 3]

    set x1o [expr $x1-2]
    set y1o [expr $y1-2]
    set x2o [expr $x2+2]
    set y2o [expr $y2+2]

    set xm [expr ($x1+$x2)/2]
    set ym [expr ($y1+$y2)/2]
    set xme [expr $xm-10]
    set yme [expr $y2o+15]

    set pp [list $x1o $y1  \
                 $x1  $y1o \
                 $xm  $y1o \
                 $xm  $y1o \
                 $x2  $y1o \
                 $x2o $y1  \
                 $x2o $ym  \
                 $x2o $ym  \
                 $x2o $y2  \
                 $x2  $y2o \
                 $xm  $y2o \
                 $xm  $y2o \
                 \
                 $xme $yme \
                 $xme $yme \
                 $xme $y2o \
                 $xme $y2o \
                 \
                 $x1  $y2o \
                 $x1o $y2  \
                 $x1o $ym  \
                 $x1o $ym ]

    set bubbleid [$c create polygon $pp -outline black -fill $color -width 1 -smooth 1 -tags "bubble"]
    $c lower $bubbleid $txtid

    set dx [expr $x-$xme]
    set dy [expr $y-$yme]

    $c move $bubbleid $dx $dy
    $c move $txtid $dx $dy

    set sp [opp_getsimoption animation_speed]
    set ad [expr int(1000 / (0.1+$sp))]
    after $ad [list $c delete $txtid $bubbleid]
}


#
# Called from Layouter::debugDraw()
#
proc layouter_debugDraw_finish {c msg} {
    # create label
    set bb [$c bbox all]
    $c create text [expr ([lindex $bb 0]+[lindex $bb 2])/2] [lindex $bb 1] -anchor n -text $msg

    # rescale to fit canvas
    set w [expr [lindex $bb 2]-[lindex $bb 0]]
    set h [expr [lindex $bb 3]-[lindex $bb 1]]
    set cw [winfo width $c]
    set ch [winfo height $c]
    set fx [expr $cw/double($w)]
    set fy [expr $ch/double($h)]
    if {$fx>1} {set fx 1}
    if {$fy>1} {set fy 1}
    $c scale all 0 0 $fx $fy

    $c config -scrollregion [$c bbox all]
    update idletasks
}

proc calibrate_animdelay {} {

   # animdelay: holds upper limit for delay `for' loop
   global animdelay

   # 100ms should be 100000 ticks on a PC -- verify that
   set t0 [clock clicks]
   after 100
   set t1 [clock clicks]
   set ms100 [expr $t1-$t0]
   if {$ms100>80000 && $ms100<120000} {set ms100 100000}


   # test proc speed: how many "clock clicks" does an empty 4000 loop take?
   set repeatcount 10
   while {1} {
      #puts "dbg: repeatcount=$repeatcount"
      set ad [expr 4000*$repeatcount]
      set t0 [clock clicks]
      for {set i 0} {$i<$ad} {incr i} {}
      set t1 [clock clicks]
      #puts "dbg: t0=$t0, t1=$t1"
      if {$t1!=$t0} break
      set repeatcount [expr 5*$repeatcount]
   }
   set loopticks [expr ($t1-$t0)/$repeatcount]
   #puts "dbg: loopticks=$loopticks"

   # calc preliminary animdelay from loopticks
   set animdelay [expr 100*$ms100/$loopticks]

   # empirical correction
   set animdelay [expr int($animdelay * sqrt($animdelay/100))]

   puts ""
   puts "Anim-speed calibrated: $animdelay"
}

# animate2 is not currently used
proc animate2 {c tag} {
    #
    # Animate2 has the advantage that several animations
    # can be started concurrently; one can synchronize on the
    # end of an animation by vwaiting on the returned variable name:
    #   set var [animate2 $c $arrow]; vwait $var
    #
    # The disadvantage is that the granularity of the after command
    # is big on a PC, so the animation will be slow.
    #
    set coords [$c coords $tag]

    set x1 [lindex $coords 0]
    set y1 [lindex $coords 1]
    set x2 [lindex $coords 2]
    set y2 [lindex $coords 3]
    set len [expr sqrt(($x2-$x1)*($x2-$x1)+($y2-$y1)*($y2-$y1))]

    set steps [expr int($len/5)]
    if {$steps==0} {set steps 1}

    set dx [expr ($x2-$x1)/$steps]
    set dy [expr ($y2-$y1)/$steps]

    draw_message $c $msgptr $x1 $y1 $msgname $msgkind

    global done$c$ball
    update idletasks
    after 10 "animate2:move $c $ball $dx $dy $steps"

    return done$c$ball
}

proc animate2:move {c ball dx dy i} {
    $c move $ball $dx $dy
    update idletasks
    incr i -1
    if {$i>0} {
       after 10 "animate2:move $c $ball $dx $dy $i"
    } else {
       $c delete $ball
       global done$c$ball
       set done$c$ball 1
    }
}


