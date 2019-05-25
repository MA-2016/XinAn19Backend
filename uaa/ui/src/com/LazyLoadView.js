import * as React from 'react';
import Panel from './Panel';
import Separator from 'react-uwp/Separator';

require('./LazyLoadView.css')

/**
 * props:
 * * title: string
 * * onScrollOverBottom(): ReactElement
 */
export default class LazyLoadView extends React.Component {

    componentDidMount() {
        const { onScrollOverBottom } = this.props;
        let beforeScrollTop = this.root.scrollTop;
        this.root.onscroll = () => {
            let direction = this.root.scrollTop - beforeScrollTop;
            // scroll down to bottom
            if (direction > 0 && Math.ceil(this.root.scrollTop) >=
                this.root.scrollHeight - this.root.clientHeight) {
                onScrollOverBottom && onScrollOverBottom()
            }
            // scroll up to top
            // else if (direction < 0 && Math.ceil(this.root.scrollTop) == 0) {
            //     onScrollOverTop && onScrollOverTop();
            // }
            beforeScrollTop = this.root.scrollTop;
        };
    }
    
    render() {
        const { title, style, children } = this.props
        return (
            <Panel className="LazyLoadView" style={{
                    boxSizing: 'border-box',
                    position: 'relative',
                    borderRadius: 5,
                    overflow: 'hidden',
                    ...style}}>
                <div style={{margin: '5px 5px 0px 5px'}}>
                    <h3>{title}</h3>
                    <Separator />
                </div>
                <div className="LazyLoadViewContainer"
                    style={{
                        width: '100%', height: 'calc(100% - 29px)',
                        padding: '0px 0px 5px 5px',
                        overflowY: 'scroll',
                    }}
                    ref={(node) => this.root = node} >
                    {children}
                </div>
            </Panel>
        )
    }
}