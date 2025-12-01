// ...existing code...
import { useState } from "react";


function App() {
  const [counter, setCounter] = useState(0);
  return (
    <>
      <h1>Press the Button!</h1>
      <button
        type="button"
        className="btn btn-secondary"
        onClick={() => setCounter(counter + 1)}
      >
        Press
      </button>

      <p>Button pressed {counter} times</p>
    </>
  );
}

export default App;
// ...existing code...