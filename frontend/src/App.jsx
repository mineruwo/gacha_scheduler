import { useState, useEffect } from 'react';
import reactLogo from './assets/react.svg';
import './App.css';

function App() {
  const [message, setMessage] = useState('');

  useEffect(() => {
    // Fetch the message from the Spring Boot backend
    fetch('http://localhost:8080/api/hello')
      .then(response => {
        if (!response.ok) {
          throw new Error('Network response was not ok');
        }
        return response.text(); // The response is plain text
      })
      .then(text => setMessage(text))
      .catch(error => {
        console.error('There was a problem with the fetch operation:', error);
        setMessage('Failed to load message from backend.');
      });
  }, []); // The empty dependency array ensures this effect runs only once

  return (
    <div className="App">
      <header className="App-header">
        <img src={reactLogo} className="logo" alt="logo" />
        <h1>React + Spring Boot Demo</h1>
        <p>
          Message from backend:
        </p>
        {/* Display the message from the backend */}
        <p className="backend-message">
          {message || 'Loading...'}
        </p>
      </header>
    </div>
  );
}

export default App;
