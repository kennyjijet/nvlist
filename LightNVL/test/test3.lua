
paragraph.start("test/test", 1); paragraph.append("Simple text"); paragraph.finish()

paragraph.start("test/test", 2); paragraph.append("Text with\tescape\ncodes[ ] \\"); paragraph.finish()

paragraph.start("test/test", 3); paragraph.append("Text with \"quotes\""); paragraph.finish()

paragraph.start("test/test", 4); paragraph.append("Text with "); embedded(); paragraph.append(" code"); paragraph.finish()
paragraph.start("test/test", 5); paragraph.append("Text with "); embedded("["); paragraph.append(" code"); paragraph.finish()
paragraph.start("test/test", 6); paragraph.append("Text with {tag a,b,c,d} embedded {/tag} text tags"); paragraph.finish()








text("Code line")


text("Multi")
text("Code")
text("Lines")


