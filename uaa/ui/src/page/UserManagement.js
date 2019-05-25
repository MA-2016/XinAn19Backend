import * as React from 'react';
import * as PropTypes from "prop-types";

import { ThemeType } from 'react-uwp/Theme';
import SplitViewCommand from 'react-uwp/SplitViewCommand';
import NavigationView from 'react-uwp/NavigationView';
import Separator from 'react-uwp/Separator';
import ReactDataGrid from 'react-data-grid';
 
import Label from '../com/Label';
import LazyLoadView from '../com/LazyLoadView';
import Field from '../com/Field';

const mockData = {
    administrator: {
        name: 'Luncert',
        lastLogon: '2018-10-9 22:30:13'
    },
    accessRecord: [...Array(30).fill(0).map(
        (numb, index) => ({
            id: 'Id' + (index + 1),
            timestamp: '2018-10-11 12:30:11',
            message: 'create application' 
        })
    )]
}

export default class UserManagement extends React.Component {

    static contextTypes = { theme: PropTypes.object }
    static context = { theme: ThemeType }

    constructor(props) {
        super(props)
        this.state = {
            viewIndex: 1
        }
    }

    changeView(index) {
        this.setState({viewIndex: index})
    }

    render() {
        const { administrator, accessRecord } = mockData
        const { theme } = this.context
        const { viewIndex } = this.state
        const fieldBaseStyle = {
            fontSize: 20,
            margin: 5
        }

        let contentView
        switch (viewIndex) {
        case 0:
            let key = 0
            let cellStyle = {
                display: 'inline-block',
                verticalAlign: 'top',
                textAlign: 'center'
            }
            contentView = [
                <h1 key={key++}>Application Account Management Platform</h1>,
                <Separator key={key++} disabled />,
                <Field key={key++} name='administrator' value={administrator.name} style={fieldBaseStyle} />,
                <br key={key++}/>,
                <Field key={key++} name='last logon' value={administrator.lastLogon} style={fieldBaseStyle} />,
                <br key={key++}/>,
                <LazyLoadView key={key++} title='Access Records'
                    // TODO:
                    onScrollOverBottom={() => {
                        for (let i = 0; i < 10; i++) {
                            accessRecord.push({
                                id: 'Id' + (i + 1),
                                timestamp: '2018-10-11 12:30:11',
                                message: 'create application' 
                            })
                        }
                        this.forceUpdate()
                    }}
                    style={{width: '100%', height: 500}}>
                    <div style={{width: '100%', height: 18}}>
                        <span style={{ width: '20%', ...cellStyle }}>ID</span>
                        <Separator direction="column" />
                        <span style={{ width: '20%', ...cellStyle }}>Timestamp</span>
                        <Separator direction="column" />
                        <span style={{ width: '58%', ...cellStyle }}>Message</span>
                    </div>
                    <Separator />
                    // TODO:
                    { accessRecord.map((v, i) => 
                        <div key={i} style={{width: '100%', height: 18}}>
                            <span style={{display: 'inline-block',
                                width: '20%', textAlign: 'center'}}>{v.id}</span>
                            <Separator direction="column" />
                            <span style={{display: 'inline-block',
                                width: '20%', textAlign: 'center'}}>{v.timestamp}</span>
                            <Separator direction="column" />
                            <span style={{display: 'inline-block',
                                width: '58%', textAlign: 'center'}}>{v.message}</span>
                        </div>
                    )}
                </LazyLoadView>
            ]
            break
        case 1:
            break
        default:
            console.error('invalid viewIndex')
            break
        }

        return (
            <NavigationView
                style={{width: '100%', height: '100%'}}
                displayMode="overlay"
                autoResize={false}
                background={theme.listLow}
                initWidth={48}
                expandedWidth={150}
                focusNavigationNodeIndex={1}
                navigationTopNodes={[
                    <SplitViewCommand label="Profile" icon="ContactLegacy"
                        onClick={() => this.changeView(0)} />,
                    <SplitViewCommand label="Application" icon="AllApps"
                        onClick={() => this.changeView(1)} />
                ]}
                navigationBottomNodes={[
                    <SplitViewCommand label="Logout" icon="ExpandTileLegacy"
                        onClick={() => console.log('logout')} />,
                    <SplitViewCommand label="Settings" icon={"\uE713"}
                        onClick={() => this.changeView(9)} />
                ]}
            >
                <div style={{ width: 'calc(100% - 48px)', height: '100%',
                    marginLeft: 48, position: 'relative',
                    padding: 10, boxSizing: 'border-box', }}
                    >
                    {contentView}
                </div>
            </NavigationView>
        )
    }
    
}