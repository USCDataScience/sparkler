import React, {Component} from 'react';
import {BrowserRouter, Route, Switch} from 'react-router-dom'
import Home from './Home'
import BananaFrame from "./BananaFrame";
import SolrFrame from "./SolrFrame";

class Main extends Component {
    render() {
        return (
            <div>
                <main>
                    <Switch>
                        <Route exact path='/' component={Home}/>
                        <Route exact path='/analytics' component={BananaFrame}/>
                        <Route exact path='/solr' component={SolrFrame}/>
                    </Switch>
                </main>
            </div>
        );
    }
}

export default Main
