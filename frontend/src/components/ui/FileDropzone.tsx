"use client";

import { useDropzone } from "react-dropzone";

export function FileDropzone({ onFile }: { onFile: (file: File) => void }) {
  const { getRootProps, getInputProps, isDragActive } = useDropzone({
    // Browsers rarely recognise the MIME type for .class files; accept all
    // common binary MIME types and validate by extension in onDropAccepted.
    accept: {
      "application/java-vm": [".class"],
      "application/octet-stream": [".class"],
      "application/x-java-class": [".class"],
    },
    maxFiles: 1,
    onDropAccepted(files) {
      const file = files[0];
      // Final extension guard in case a wrong MIME type slips through
      if (!file.name.endsWith(".class")) return;
      onFile(file);
    },
  });

  return (
    <div
      {...getRootProps()}
      className="card cursor-pointer border-dashed p-10 text-center transition hover:border-accent"
    >
      <input {...getInputProps()} />
      <p className="text-lg text-slate-100">
        {isDragActive ? "Drop .class file here" : "Drag and drop .class file"}
      </p>
      <p className="mt-2 text-sm text-slate-400">
        Only compiled Java bytecode class files are accepted.
      </p>
    </div>
  );
}
