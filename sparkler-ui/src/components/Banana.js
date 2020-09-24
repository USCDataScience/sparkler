import React, {Component} from 'react'
import Iframe from "react-iframe";

class Banana extends Component{

    render() {
        return (
            <div>
                <Iframe url="/banana" width="100%" height="1200px"/>
            </div>
        );
    }
}

export default Banana