import React from 'react';
import logo from './logo.svg';
import './App.css';
import ToiletList from "./components/ToiletList";

function App() {
  return (
    <div className="App">
      <header className="App-header">
        <img src={logo} className="App-logo" alt="logo"/>
        <ToiletList/>
      </header>
    </div>
  );
}

export default App;
