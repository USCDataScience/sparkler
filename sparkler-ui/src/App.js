import React, {Component} from 'react';
import './App.css';
import Main from './views/Main'
import Navbar from './components/Navbar'


//const nano = require('nano')('http://admin:password@localhost:5984');
//nano.db.create('alice');


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
