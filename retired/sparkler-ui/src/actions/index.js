import * as types from '../reducers/types';
import * as env from '../utils/constants'
import axios from 'axios';
import fileDownload from "js-file-download";

export const fetchModel = (model) =>{
    window.open(env.API_URL+'/explorer-api/classify/download/'+model, '_blank');
    return (dispatch) => {
                let r = {
                    type: types.DOWNLOADED,
                    payload: ""
                }
                dispatch(r)
    }
}
export const fetchSeeds = (model) => {
    return (dispatch) => {
        axios.get(env.API_URL+"/explorer-api/cmd/seed/fetch/"+model).then(
            response => {
                let r = {
                    type: types.SEED_URLS_LIST,
                    payload: response.data
                }
                dispatch(r)
            }
        )
    }
}
export const fetchConfig = (model) => {
    return (dispatch) => {
        axios.get(env.API_URL+"/explorer-api/cmd/crawler/settings/"+model).then(
            response => {
                let r = {
                    type: types.NEW_CONFIG,
                    payload: response.data
                }
                dispatch(r)
            }
        )
    }
}

export const updateCrawlConfig = (model, config) => {
    return (dispatch) => {
        axios.post(env.API_URL+"/explorer-api/cmd/crawler/settings/"+model, config).then(
            response => {
                let r = {
                    type: types.NEW_CONFIG,
                    payload: response.data
                }
                dispatch(r)
            }
        )
    }
}
export const exportData = (model) => {
    return (dispatch) => {
        axios.get("/solr/crawldb/select?indent=on&q=crawl_id:"+model+"&wt=json").then((
            response => {
                fileDownload(JSON.stringify(response.data, null, 2), 'export.json');
                dispatch({type: types.DATA_EXPORTED, payload: "OK"})
            }
        ))
    }
}
export const createNewModel = (name) => {
    return (dispatch) => {
        axios.get(env.API_URL+"/explorer-api/classify/createnew/"+name)
            .then(response => {
                response = {
                    type: types.NEW_MODEL,
                    payload: name
                }
                dispatch(response)
                try {
                    const serializedState = JSON.stringify({current_model: name});
                    localStorage.setItem('state', serializedState);
                } catch {
                    // ignore write errors
                }
                response = {
                    type: types.ENABLE_MODEL,
                    payload: name

                };
                dispatch(response)

            })
    }
}

export const updateModel = (name, annotations) => {
    return (dispatch) => {
        axios.post(env.API_URL+"/explorer-api/classify/update/"+name, annotations)
            .then(response => {
                response = {
                    type: types.MODEL_STATS,
                    payload: response.data
                }
                dispatch(response)
            })
    }
}
export const fetchAllModels = () => {
    return (dispatch) => {
        axios.get(env.API_URL+"/explorer-api/classify/listmodels")
            .then(response => {
                response = {
                    type: types.MODEL_LIST,
                    payload: response.data
                }
                dispatch(response)
            })
    }
}

export const enableModel = (name) => {
    return (dispatch) => {
        try {
            const serializedState = JSON.stringify({current_model: name});
            localStorage.setItem('state', serializedState);
        } catch {
            // ignore write errors
        }
        let response = {
            type: types.ENABLE_MODEL,
            payload: name

        };
        dispatch(response)
    }
}
export const fetchNewTime = () => ({
    type: types.FETCH_NEW_TIME,
    payload: new Date().toString(),
})


export const searchFired = (b) => ({
    type: types.SEARCH_FIRED,
    payload: b
})

export const searchWebsites = (model, search_term) => {
    return (dispatch) => {
        axios.get(env.API_URL+"/explorer-api/search/"+model+"/" + search_term)
            .then(response => {
                let jdata = response.data;
                console.log(jdata)
                response = {
                    type: types.SEARCH_RESULTS,
                    payload: jdata
                };
                dispatch(response)
            }).then(response => {
                dispatch(searchFired(false))
            })
            .catch(error => {
                throw(error);
            })

    }
};

export const saveSeedURLs = (model,urls) => {

    return (dispatch) => {
        axios.post(env.API_URL+'/explorer-api/cmd/seed/upload/'+model, urls)
            .then(response => {
                response = {
                    type: types.SEED_URLS_LIST,
                    payload: response.data

                }
                dispatch(response)
            })
            .catch(error => {
                throw(error);
            })
    }

};

export const modelStats = (model) => {
    return (dispatch) => {
        axios.get(env.API_URL+'/explorer-api/classify/stats/'+model)
            .then(response => {
                response = {
                    type: types.MODEL_STATS,
                    payload: response.data
                }

                dispatch(response)
            })
    }
}

let relevancy = {"page1-ann":0,"page2-ann":0,"page3-ann":0,"page4-ann":0,"page5-ann":0,"page6-ann":0,"page7-ann":0,"page8-ann":0,"page9-ann":0,"page10-ann":0,"page11-ann":0,"page12-ann":0,};
export const setRelevancy = (frame, val) => {
    return (dispatch) => {
        relevancy[frame] = val;
        console.log(relevancy);
        let response = {
            type: types.UPDATED_RELEVANCY,
            payload: relevancy
        }
        dispatch(response)
    }
}


export const startCrawl = (model, obj) => {
    return (dispatch) => {
        axios.post(env.API_URL+'/explorer-api/cmd/crawler/crawl/'+model, obj)
            .then( response => {
                let r = {
                    type: types.CRAWL_STATUS,
                    payload: types.CRAWL_STARTING
                }
                dispatch(r)

                }
            )
    }
}

export const killCrawl = (model) => {
    return (dispatch) => {
        axios.delete(env.API_URL+'/explorer-api/cmd/crawler/crawl/'+model)
            .then( response => {
                    let r = {
                        type: types.CRAWL_STATUS,
                        payload: types.CRAWL_FINISHED
                    }
                    dispatch(r)

                }
            )
    }
}


function getCompleted(model){
    axios.get(env.API_URL+'/explorer-api/cmd/crawler/crawler/'+model)
        .then(response =>{
            let t = null;
            let s = getStatus(response.data, model);


            return s === "COMPLETED";

        })

    return false;
}

export const crawlStatus =  (model) => {
    return (dispatch) => {
        axios.get(env.API_URL+'/explorer-api/cmd/crawler/crawler/'+model)
            .then(response =>{
                let t = null;

                if ('running' in response.data){
                    if(response.data.running === "true"){
                        t = types.CRAWL_STARTED
                        response = {
                            type: types.CRAWL_STATUS,
                            payload: t
                        }

                        dispatch(response)
                    }
                    else{
                        t = types.CRAWL_FINISHED
                        response = {
                            type: types.CRAWL_STATUS,
                            payload: t
                        }

                        dispatch(response)
                    }
                }
                else {
                    let s = getStatus(response.data, model);

                    if (s === "RUNNING") {
                        t = types.CRAWL_STARTED
                    } else if (s === "STARTING") {
                        t = types.CRAWL_STARTING
                    } else if (s === "COMPLETED") {
                        t = types.CRAWL_FINISHED
                    } else {
                        t = types.CRAWL_STARTED
                    }

                    response = {
                        type: types.CRAWL_STATUS,
                        payload: t
                    }

                    dispatch(response)
                }
            })
    }
}

function getStatus(data, model) {

    for (let key in data.items) {
        if (data.items.hasOwnProperty(key)) {
            console.log(key + " -> " + data.items[key]);
            let item = data.items[key];
            let c = item.spec.containers[0];
            if(c.name === model+"crawl"){
                if(item.status.hasOwnProperty("containerStatuses")){
                    if(item.status.containerStatuses[0].hasOwnProperty("state")){
                        let state = item.status.containerStatuses[0].state;
                        for(let k in state){
                            if(k==="waiting"){
                                return "STARTING"
                            }
                            else if(k==="terminated"){
                                return "COMPLETED"
                            }
                        }


                    }
                }
                else{
                    return "STARTING"
                }
            }
        }
    }

}
/*
export const login = (user) => ({
    type: types.LOGIN,
    payload: user
})

export const logout = () => ({
    type: types.LOGOUT,
})*/
