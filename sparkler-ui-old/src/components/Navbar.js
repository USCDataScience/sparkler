import React, {Component} from 'react';
import {connect} from 'react-redux';
import {fetchNewTime} from "../actions";
import ModelDialog from "./dialogs/ModelDialog";
import {RouterButton} from "./RouterButton";
import {Link, withRouter} from "react-router-dom";


class Navbar extends Component {


    constructor(props) {
        super(props);
        this.modalElement = React.createRef();

    }

    handle_models = () => this.modalElement.current.handleOpen();

    render(){
        const { history } = this.props;

        return(
            <nav className="bp3-navbar bp3-dark">
                <div style={{margin: "0 auto", width: "600px"}}>
                    <div className="bp3-navbar-group bp3-align-left">
                        <div className="bp3-navbar-heading">Domain Discovery - Seed Generation{/* - Current time: {this.props.currentTime}*/}</div>
                    </div>
                    <div className="bp3-navbar-group bp3-align-right">
                        <RouterButton label="Explorer" activeOnlyWhenExact={false} history={history} to={"/"} className="bp3-button bp3-minimal bp3-icon-home">Explorer</RouterButton>
                        <button className="bp3-button bp3-minimal bp3-icon-cog" onClick={this.handle_models}>Models</button>
                        <RouterButton label="Analytics" activeOnlyWhenExact={false} history={history} to={"/analytics"} className="bp3-button bp3-minimal bp3-icon-cog" onClick={this.handle_models}>Analytics</RouterButton>
                        <ModelDialog ref={this.modalElement}/>
                    </div>
                </div>
            </nav>
        )
    }
}

const mapStateToProps = state => {
    return {
        currentTime: state.timereducer.currentTime
    }
}

const mapDispatchToProps = dispatch => ({
    updateTime: () => dispatch(fetchNewTime())
})

export default connect(mapStateToProps, mapDispatchToProps)(withRouter(Navbar));