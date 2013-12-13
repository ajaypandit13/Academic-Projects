#!/usr/bin/env python

###### Importing additional libraries
import os.path
from porterStemmer import PorterStemmer
from collections import defaultdict

###### Parsing the CACM html files and creating a single file
cacmfile = open("cacm.txt", "r").readlines()
parse_output = open("parse_output.txt","w")
print "Started Parsing for CACM File.........please wait"
for line in cacmfile:
   if ".I" in line.strip():
       line = line.split()
       parse_output.write(line[1] +"\n")
   elif ".T" in line:
       pass
   elif ".B" in line:
       pass
   elif ".A" in line:
       pass
   elif ".N" in line:
       pass
   elif ".W" in line:
       pass
   elif ".K" in line:
       pass
   elif ".C" in line:
       pass
   elif ".X" in line:
       pass
   elif line.startswith(tuple('0123456789')):
       pass
   else:
       parse_output.write(line)
parse_output.close()
print "Parsing for CACM file Completed"

print "Starting to create dictionary.........please wait"
dict_map = {}
word_list = []
parsed_file_input = open ("parse_output.txt","r").readlines()
for line in parsed_file_input:
  if line.startswith(tuple('0123456789')):
    key = int (line)
    word_list = []
  else:
    word_list.append (line.strip().lower())
    dict_map[key] = word_list
print "Completed creating dictionary"

print "Stemming and Stopping words in the dictionary.........please wait"
stop_word_file = open ("stop_words1.txt","r").read().split()
for key in dict_map.keys():
  temp_key_text = dict_map[key]
  key_text = ""
  for text in temp_key_text:
    key_text += " " + text

  #defining punctuations to be replaced by space
  punct_list_1 = [",","-","=","/","\\","'",";","^","+","|",":","<",">","`","&","(",")"]
  #defining punctuations to be eliminated
  punct_list_2 = [".",'"',"[","]","?","!","*","%","{","}","$"]

  #removing punctuations
  for punct in punct_list_1:
    if punct in key_text:
      key_text = key_text.replace (punct, " ")
  for punct in punct_list_2:
    if punct in key_text:
      key_text = key_text.replace (punct, "")

  key_text = key_text.split()
  #removing stop words
  text_wo_stop_punct = [x for x in key_text if x not in stop_word_file]
  p = PorterStemmer()
  midlist = [(p.stem (word, 0, (len (word) - 1))) for word in text_wo_stop_punct]
  newlist = [x for x in midlist if x not in stop_word_file]
  finaltext = ''.join (" " + x for x in newlist)
  dict_map[key] = finaltext.strip()
print "Completed stemming and stopping"

dict_word_ID_map = {}
i = 1
print "Assigning IDs to words..........please wait"
for key in dict_map.keys():
  for word in dict_map[key].split():
    if dict_word_ID_map.has_key (word):
      pass
    else:
      dict_word_ID_map[word] = i
      i += 1
print "completed assigning IDs to words"

wordcount = defaultdict (int)
print "creating additional files.......please wait"
for key in dict_map.keys():
  temp = dict_map[key].split()
  doclen = len (temp)
  for word in temp:
    wordcount[word] += 1

  for word in wordcount.keys():
    #print "processing word: %s ID: %s" % (word, dict_word_ID_map[word])
    if word == 'con':
      continue
    if word == 'of\x19':
      continue
    if word == 'redundant\x19':
      continue
    
    if os.path.exists ("terms/" +word):
      append = open ("terms/"+word, "a")
      docid = str(key)
      tf = str(wordcount[word])
      termid = str(dict_word_ID_map[word])
      doclen = str(doclen)
      append.write(docid)
      append.write(" ")
      append.write(tf)
      append.write(" ")
      append.write(word)
      append.write(" ")
      append.write(termid)
      append.write(" ")
      append.write(doclen)
      append.write("\n")
      append.close()
    else:
      append = open ("terms/"+word, "w")
      docid = str(key)
      tf = str(wordcount[word])
      termid = str(dict_word_ID_map[word])
      doclen = str(doclen)
      append.write(docid)
      append.write(" ")
      append.write(tf)
      append.write(" ")
      append.write(word)
      append.write(" ")
      append.write(termid)
      append.write(" ")
      append.write(doclen)
      append.write("\n")
      append.close()
  wordcount.clear() 
print "files creation completed"

filenames = os.listdir('terms/')
newindex = open ("main_index.txt","a")
file2 = open ("file2.txt","a")
print "writing to helper file2 => TermID DocID TermFreq........please wait"
for f in filenames:
   data = open ("terms/"+f, "r")
   for line in data:
      newindex.write(line)
      line = line.split()
      file2.write(line[3])
      file2.write(" ")
      file2.write(line[2])
      file2.write(" ")
      file2.write(line[0])
      file2.write(" ")
      file2.write(line[1])
      file2.write("\n")
   data.close()
file2.close()
newindex.close()
print "completed writing to file2"

doc3 = {}
file3 = open ("file3.txt","a")
print "writing to helper file3 => DocID DocName Doclen.........please wait"
final = open ("main_index.txt", "r").readlines()
for line in final:
   line = line.split()
   docid = line[0]
   tf = line[1]
   termid = line[3]
   doclen = line[4]
   if doc3.has_key(docid):
      pass
   else:
      file3.write(docid + " " + "CACM-"+docid + " " + doclen + "\n")
      doc3[docid] = ("CACM-"+docid,doclen)
file3.close()
print "completed witing to file3"

termdict = defaultdict(list)
termfile = open ("file2.txt","r").readlines()
offset = 1
ctf = 1
df = 1
startoffset = 0
file1 = open ("file1.txt", "a")
print "writing to helper file1 => Term TermID Offset CTF DF.......please wait"
for x in xrange(len(termfile) - 1):
   line = termfile[x]
   nextline = termfile[x+1]
   line = line.strip()
   line = line.split()
   nextline = nextline.strip()
   nextline = nextline.split()
   nexttermid = nextline[0]
   nextterm = nextline[1]
   termid = line[0]
   term = line[1]
   if (termid == nexttermid):
      offset += 1
      ctf += int(line[3])
      df += 1
   else:
      file1.write(term)
      file1.write(" ")
      file1.write(termid)
      file1.write(" ")
      file1.write(str(offset-df+1))
      file1.write(" ")
      file1.write(str(offset))
      file1.write(" ")
      file1.write(str(ctf))
      file1.write(" ")
      file1.write(str(df))
      file1.write("\n")
      ctf = int(nextline[3])
      df = 1
      offset += 1
file1.close()

avgdoclen = 0
for word,doclen in doc3.values():
   avgdoclen += int(doclen)
avgdoclen = avgdoclen/float(len(doc3.keys()))
print avgdoclen

print "completed writing to file1"
print "completed indexing process"
newindex.close()
