import React, { useEffect, useState } from "react";
import { createRoot } from "react-dom/client";
import "./styles.css";
const DOCUMENT_API = `${import.meta.env.VITE_API_BASE_URL || "/api"}/documents`;
const DOCUMENT_LIST_API = `${DOCUMENT_API}/getAllDocuments`;
const types = ["FINANCIAL_STATEMENT", "INSURANCE_POLICY", "OTHER"];
function Status({ value }) {
  return (
    <span className={`status ${value.toLowerCase()}`}>
      {value.replace("_", " ")}
    </span>
  );
}
function App() {
  const [docs, setDocs] = useState([]),
    [status, setStatus] = useState(""),
    [type, setType] = useState(""),
    [selected, setSelected] = useState(null),
    [history, setHistory] = useState([]),
    [loading, setLoading] = useState(true),
    [message, setMessage] = useState("");
  const load = async (showLoadingIndicator = true) => {
    if (showLoadingIndicator) {
      setLoading(true);
    }
    try {
      let q = new URLSearchParams();
      if (status) q.set("status", status);
      if (type) q.set("documentType", type);
      let r = await fetch(`${DOCUMENT_LIST_API}?${q}`);
      if (!r.ok) {
        throw new Error("Unable to retrieve documents");
      }
      let d = await r.json();
      setDocs(Array.isArray(d.content) ? d.content : []);
      setMessage((currentMessage) =>
        currentMessage === "Unable to load documents. Check that the API is running."
          ? ""
          : currentMessage,
      );
    } catch {
      setMessage("Unable to load documents. Check that the API is running.");
    } finally {
      if (showLoadingIndicator) {
        setLoading(false);
      }
    }
  };
  useEffect(() => {
    load();
    const i = setInterval(() => load(false), 2500);
    return () => clearInterval(i);
  }, [status, type]);
  const open = async (id) => {
    let [d, h] = await Promise.all([
      fetch(`${DOCUMENT_API}/${id}`).then((x) => x.json()),
      fetch(`${DOCUMENT_API}/${id}/history`).then((x) => x.json()),
    ]);
    setSelected(d);
    setHistory(h);
  };
  return (
    <main>
      <header>
        <div>
          <h1>Document processing</h1>
          <p className="muted">Upload, validate, and track broker documents.</p>
        </div>
        <Upload
          onDone={(text) => {
            setMessage(text);
            load();
          }}
        />
      </header>
      {message && <div className="notice">{message}</div>}
      <section className="toolbar">
        <h2>Documents</h2>
        <select value={status} onChange={(e) => setStatus(e.target.value)}>
          <option value="">All statuses</option>
          {["UPLOADED", "PROCESSING", "PROCESSED", "FAILED"].map((x) => (
            <option key={x}>{x}</option>
          ))}
        </select>
        <select value={type} onChange={(e) => setType(e.target.value)}>
          <option value="">All types</option>
          {types.map((x) => (
            <option key={x}>{x}</option>
          ))}
        </select>
      </section>
      {loading ? (
        <p>Loading documents…</p>
      ) : docs.length === 0 ? (
        <div className="empty">No documents match these filters.</div>
      ) : (
        <section className="grid">
          {docs.map((d) => (
            <button
              className="card"
              key={d.documentId}
              onClick={() => open(d.documentId)}
            >
              <Status value={d.status} />
              <h3>{d.filename}</h3>
              <p>
                {d.documentId} · {d.documentType.replace("_", " ")}
              </p>
              <small>Uploaded {new Date(d.createdAt).toLocaleString()}</small>
            </button>
          ))}
        </section>
      )}
      {selected && (
        <Detail
          document={selected}
          history={history}
          close={() => setSelected(null)}
        />
      )}
    </main>
  );
}
function Upload({ onDone }) {
  const [file, setFile] = useState(null),
    [type, setType] = useState(types[0]),
    [busy, setBusy] = useState(false);
  const submit = async (e) => {
    e.preventDefault();
    if (!file) return;
    setBusy(true);
    let f = new FormData();
    f.append("file", file);
    f.append("documentType", type);
    try {
      let r = await fetch(DOCUMENT_API, { method: "POST", body: f });
      let body = await r.json();
      onDone(
        r.ok
          ? body.duplicateUpload
            ? `This file was already uploaded as ${body.filename}.`
            : `Upload accepted: ${body.documentId}`
          : body.message || "Upload failed",
      );
    } catch {
      onDone("Upload failed. Please try again.");
    } finally {
      setBusy(false);
    }
  };
  return (
    <form className="upload" onSubmit={submit}>
      <input
        aria-label="PDF document"
        type="file"
        accept="application/pdf"
        onChange={(e) => setFile(e.target.files[0])}
      />
      <select value={type} onChange={(e) => setType(e.target.value)}>
        {types.map((x) => (
          <option key={x} value={x}>
            {x.replace("_", " ")}
          </option>
        ))}
      </select>
      <button disabled={!file || busy}>
        {busy ? "Uploading…" : "Upload PDF"}
      </button>
    </form>
  );
}
function Detail({ document: d, history, close }) {
  return (
    <div className="overlay">
      <article className="detail">
        <button className="close" onClick={close}>
          ×
        </button>
        <Status value={d.status} />
        <h2>{d.filename}</h2>
        <p className="muted">
          {d.documentId} · {d.documentType.replace("_", " ")} ·{" "}
          {d.processingAttempts} attempt(s)
        </p>
        {d.failureReason && (
          <div className="error">
            <b>{d.failureReason}</b>
            {d.validationErrors && <p>{d.validationErrors}</p>}
          </div>
        )}
        <h3>Extracted information</h3>
        {d.result ? (
          <dl>
            {Object.entries(d.result).map(([k, v]) => (
              <React.Fragment key={k}>
                <dt>{k.replace(/([A-Z])/g, " $1")}</dt>
                <dd>{String(v)}</dd>
              </React.Fragment>
            ))}
          </dl>
        ) : (
          <p className="muted">
            Results will appear once processing completes.
          </p>
        )}
        <h3>Processing history</h3>
        <ol className="timeline">
          {history.map((h, i) => (
            <li key={i}>
              <Status value={h.status} />
              <span>
                {new Date(h.timestamp).toLocaleString()}{" "}
                {h.reason && `— ${h.reason}`}
              </span>
            </li>
          ))}
        </ol>
      </article>
    </div>
  );
}
createRoot(document.getElementById("root")).render(<App />);
