import React, {Component} from 'react';
import {Button, Card, InputGroup} from "@blueprintjs/core";
import {FormGroup} from "@blueprintjs/core/lib/cjs";
import {INTENT_PRIMARY} from "@blueprintjs/core/lib/cjs/common/classes";
import {createNewModel} from "../../actions";
import {connect} from "react-redux";

class NewModel extends Component {


    constructor(props) {
        super(props);
        this.state = {};
        this.state['intent'] = INTENT_PRIMARY;
        this.state['model']  = '';

        this.handleChange = this.handleChange.bind(this);
        this.handleSubmit = this.handleSubmit.bind(this);
    }

    handleSubmit = function(e){

        this.props.createNewModel(this.state.model)


    };

    handleChange = function(e){
        this.setState({model:e.target.value})
    };

    render() {
        return <Card>
            <FormGroup
                intent={this.state.intent}
                label={"Model Name"}
                labelFor={"text-input"}>
                <InputGroup type="text" id="text-input" value={this.state.model} onChange={this.handleChange}
                            leftIcon="new-text-box" placeholder="Enter your model name" intent={this.state.intent}/>
                <Button style={{marginTop: "10px"}} onClick={() => this.handleSubmit()}>Create Model</Button>

            </FormGroup>
        </Card>
    }
}

const mapDispatchToProps = dispatch => ({
    createNewModel: (s) => dispatch(createNewModel(s))
})

export default connect(null, mapDispatchToProps)(NewModel)
