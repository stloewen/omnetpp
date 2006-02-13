##
## Name:
##
##   create_seeds
##
## Description:
##
##   This function generates a vector of orthogonal seeds suitable for
##   simulation replications. The seeds are generated by the omnetpp
##   seedtool utility and piped into a vector, which is returned as the
##   result to the caller.
##
## Parameters:
##
##   o_prefix
##   A string containing the omnetpp directory path. For example a typical
##   path stiring may be "/usr/home/user1/omnetpp".
##
##   num_seeds
##   A scalar parameter specifying the number of seeds to be generated.
##
##   distance
##   An scalar parameter specifying the distance between the seeds.
##
##   init_seed
##   An scalar parameter specifying the starting seed for the seed tool.
##
## Return:
##
##   A vector containing 'num_seeds' elements. Each element is a seed.
##


function seeds = create_seeds(o_prefix, num_seeds, distance, init_seed)

  ## create the command line
  seed_tool = [o_prefix, "/src/utils/seedtool"];
  com_line = sprintf("%s g %d %d %d", seed_tool, init_seed, distance, \
       num_seeds);

  ## run omnetpp seedtool and pipe output here
  pidin = popen(com_line, "r");

  ## parse the seed values from the incoming stream
  loop = 0;
  while (isstr(s = fgets(pidin)))
    seeds(++loop) = sscanf(s,"%d","C");
  endwhile

  ## close the pipe
  pclose(pidin);

endfunction
