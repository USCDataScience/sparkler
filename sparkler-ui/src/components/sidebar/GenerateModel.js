import React, {Component} from 'react';
import {Button, FormGroup, H5, H6, Icon, InputGroup} from "@blueprintjs/core";
import {INTENT_PRIMARY} from "@blueprintjs/core/lib/cjs/common/classes";
import {fetchNewTime, modelStats, searchFired, searchWebsites, updateModel} from "../../actions";
import {connect} from "react-redux";

class GenerateModel extends Component {


    constructor(props, context) {
        super(props, context);
        this.state = {}
        this.state['intent'] = INTENT_PRIMARY;
        this.state['searchterm'] = ""
        this.handleChange = this.handleChange.bind(this);
        this.generateUpdateModel = this.generateUpdateModel.bind(this)

    }

    componentDidUpdate(prevProps) {
        if (prevProps.model_stats !== this.props.model_stats) {
            alert("Model Updated")
        }

        if (prevProps.current_model !== this.props.current_model) {
            this.props.modelStats(this.props.current_model)
        }
    }

    generateUpdateModel() {
        this.props.updateModel(this.props.current_model, this.props.annotations)
    }

    handleChange(event) {
        this.setState({searchterm: event.target.value});
    }

    handleSearch(event) {
        if (this.props.current_model) {
            this.props.searchTriggered();
            this.props.searchWebsites(this.props.current_model, this.state.searchterm);
        } else {
            alert("Please Select a Model")
        }
    }

    render() {
        return (
            <div>
                <H5>Generate a Model</H5>

                <FormGroup id={"searchform"} contentClassName={"generatemodel"}
                           helperText={"Search for websites with these terms..."}
                           intent={this.state.intent}
                           label={"Search terms"}
                           labelFor={"text-input"}>
                    <InputGroup id={"searchterms"} type="search" value={this.state.searchterm}
                                onChange={this.handleChange} onKeyPress={event => {
                        if (event.key === 'Enter') {
                            this.handleSearch()
                        }
                    }} leftIcon="search" placeholder="Enter terms here"
                                intent={this.state.intent}/>
                    <Button disabled={!this.props.current_model} onClick={() => this.handleSearch()}>Go!</Button>
                </FormGroup>

                <div>
                    <H6>Minimum 10 Each</H6>
                    <table style={{textAlign: "center"}}>
                        <tr>
                            <td>{(this.props.model_stats && this.props.model_stats["2"]) || 0}</td>
                            <td>{(this.props.model_stats && this.props.model_stats["1"]) || 0}</td>
                            <td>{(this.props.model_stats && this.props.model_stats["0"]) || 0}</td>
                        </tr>
                        <tr>
                            <td>
                                <button className={"btn-circle btn-padding green"}><Icon icon={"heart"}
                                                                                         iconSize={Icon.SIZE_LARGE}/>
                                </button>
                            </td>
                            <td>
                                <button className={"btn-circle btn-padding amber"}><Icon icon={"tick"}
                                                                                         iconSize={Icon.SIZE_LARGE}/>
                                </button>
                            </td>
                            <td>
                                <button className={"btn-circle btn-padding red"} onClick={this.props.updateTime}><Icon
                                    icon={"minus"} iconSize={Icon.SIZE_LARGE}/></button>
                            </td>
                        </tr>
                    </table>

                </div>
                <div>
                    <span><Button onClick={this.generateUpdateModel} disabled={!this.props.current_model}
                                  icon={"export"}>Update Model</Button></span>
                </div>
            </div>
        )
    }
}

const mapDispatchToProps = dispatch => ({
    updateTime: () => dispatch(fetchNewTime()),
    searchWebsites: (m, s) => dispatch(searchWebsites(m, s)),
    searchTriggered: () => dispatch(searchFired(true)),
    updateModel: (m, a) => dispatch(updateModel(m, a)),
    modelStats: (m) => dispatch(modelStats(m))
})

const mapStateToProps = state => {
    return {
        current_model: state.modelreducer.current_model,
        annotations: state.modelreducer.updated_relevancy,
        model_stats: state.modelreducer.model_stats
    }
}

export default connect(mapStateToProps, mapDispatchToProps)(GenerateModel);
