import React, {Component} from 'react';
import './App.css';
import Main from './views/Main'
import Navbar from './components/Navbar'

class App extends Component {

    render() {
        return (
            <div>
                <Navbar/>
                <Main/>
            </div>
        );
    }
}

export default App;
