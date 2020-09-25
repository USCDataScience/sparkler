import React, {Component} from 'react';
import {Button, Classes, Dialog, Tab, Tabs} from "@blueprintjs/core";
import ListModels from "../panels/ListModels";
import NewModel from "../panels/NewModel";
import {connect} from "react-redux";

class ModelDialog extends Component {

    constructor(props) {
        super(props);
        this.state = {
            value: ''
        }
        this.handleChange = this.handleChange.bind(this);

    }

    componentDidUpdate(oldProps) {
        const newProps = this.props
        if (oldProps.current_model !== newProps.current_model) {
            this.handleClose()
        }
    }


    handleClose = () => this.setState({isOpen: false});
    handleOpen = () => this.setState({isOpen: true});

    handleChange(event) {
        this.setState({value: event.target.value});
    }

    render() {
        return (
            <Dialog
                icon="search-text"
                onClose={this.handleClose}
                title="Model Administration"
                {...this.state}
            >
                <div className={Classes.DIALOG_BODY} style={{minHeight: "200px"}}>

                    <Tabs id={"modeltabs"}>
                        <Tab id={"existingmodels"} title={"Existing Models"} panel={<ListModels/>}/>
                        <Tab id={"newmodel"} title={"New Model"} panel={<NewModel close={this.handleClose}/>}/>
                    </Tabs>
                </div>
                <div className={Classes.DIALOG_FOOTER}>
                    <div className={Classes.DIALOG_FOOTER_ACTIONS}>
                        <Button onClick={this.handleClose}>Close</Button>

                    </div>
                </div>
            </Dialog>

        )
    }

}

const mapStateToProps = state => {
    return {
        current_model: state.modelreducer.current_model
    }
}

export default connect(mapStateToProps, null, null, {forwardRef: true})(ModelDialog)
