package cz.kebrt.html2latex.io;

import java.io.IOException;

import cz.kebrt.html2latex.exception.FatalErrorException;

public interface IReader {
	public int read() throws IOException;
	
	public void close() throws FatalErrorException;
}
