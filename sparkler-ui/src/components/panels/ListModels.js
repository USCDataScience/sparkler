import React, {Component} from 'react';
import {Card} from "@blueprintjs/core";
import {enableModel, fetchAllModels} from "../../actions";
import {connect} from "react-redux";

class ListModels extends Component {

    componentWillMount() {
        this.props.fetchAllModels()
    }

    handleSelect = function(name){
        this.props.enableModel(name)
    }

    render() {
        if (this.props.models) {
        return (
            <Card>
                {
                    this.props.models.map((item, key) => {
                        return <li key={key}><button onClick={() => this.handleSelect(item.name)}>{item.name}</button></li>
                    })}
            </Card>
        )
    }
    else {
        return (
            <Card>No Models Available Yet</Card>
        )
}

    }
}

const mapDispatchToProps = dispatch => ({
    fetchAllModels: () => dispatch(fetchAllModels()),
    enableModel: (name) => dispatch(enableModel(name))

})

const mapStateToProps = state => {
    return {
        models: state.modelreducer.models
    }
}

export default connect(mapStateToProps, mapDispatchToProps)(ListModels)