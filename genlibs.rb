#!/usr/bin/ruby

Dir.glob("libs/*.jar") do |f|
  print "<zipfileset src=\""+f+"\"/>\n"
end