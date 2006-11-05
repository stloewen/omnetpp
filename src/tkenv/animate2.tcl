#=================================================================
#  ANIMATE2.TCL - part of
#
#                     OMNeT++/OMNEST
#            Discrete System Simulation in C++
#
#=================================================================

#----------------------------------------------------------------#
#  Copyright (C) 1992-2005 Andras Varga
#
#  This file is distributed WITHOUT ANY WARRANTY. See the file
#  `license' for details on this and other legal matters.
#----------------------------------------------------------------#


#
# Perform concurrent animations
#
proc do_concurrent_animations {animjobs} {
    global config

    foreach job $animjobs {
        puts "DOING: $job"
        #set config(concurrent-anim) 0
        #eval $job
        #set config(concurrent-anim) 1
    }

    # we should form groups -- anim requests within the group are performed
    # concurrently. group1: first request of each sending; group2: second request
    # of each sending, etc. One group may contain animations on several
    # compound modules.

    #XXX right now, we just lump everything together in a single group
    do_animate_group $animjobs
    puts "----"
}

proc do_animate_group {animjobs} {
}

