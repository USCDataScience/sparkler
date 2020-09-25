import React, {Component} from 'react';
import {BrowserRouter, Route, Switch} from 'react-router-dom'
import Home from './Home'
import BananaFrame from "./BananaFrame";

class Main extends Component {
    render() {
        return (
            <div>
                <main>
                    <Switch>
                        <Route exact path='/' component={Home}/>
                        <Route exact path='/analytics' component={BananaFrame}/>
                    </Switch>
                </main>
            </div>
        );
    }
}

export default Main