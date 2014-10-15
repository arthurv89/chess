package nl.arthurvlug.chess;

import javax.security.auth.login.Configuration;

import org.apache.crunch.DoFn;
import org.apache.crunch.Emitter;
import org.apache.crunch.PCollection;
import org.apache.crunch.PTable;
import org.apache.crunch.Pipeline;
import org.apache.crunch.impl.mr.MRPipeline;
import org.apache.crunch.types.writable.Writables;

public class WordCount {
	public static void main(String[] args) {
		// Create an object to coordinate pipeline creation and execution.
		Pipeline pipeline = new MRPipeline(WordCount.class);
		
		// Reference a given text file as a collection of Strings.
		PCollection<String> lines = pipeline.readTextFile(args[1]);

		// Define a function that splits each line in a PCollection of Strings
		// into
		// a
		// PCollection made up of the individual words in the file.
		PCollection<String> words = lines.parallelDo(new DoFn<String, String>() {
			public void process(String line, Emitter<String> emitter) {
				for (String word : line.split("\\s+")) {
					emitter.emit(word);
				}
			}
		}, Writables.strings()); // Indicates the serialization format

		// The count method applies a series of Crunch primitives and returns
		// a map of the unique words in the input PCollection to their counts.
		// Best of all, the count() function doesn't need to know anything about
		// the kind of data stored in the input PCollection.
		PTable<String, Long> counts = words.count();

		// Instruct the pipeline to write the resulting counts to a text file.
		pipeline.writeTextFile(counts, args[2]);
		// Execute the pipeline as a MapReduce.
		pipeline.done();
	}
}