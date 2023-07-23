% Read vechicle counts and objective values
alphaobj = dlmread('objectivesJerry.txt');
jerryA=dlmread('ambulancesJerry.txt');
jerryH=dlmread('helicoptersJerry.txt');

% Plot objective values against savings coefficient
plot(alphaobj(:,1),alphaobj(:,2),"k*:",'LineWidth',1.5,'MarkerSize',5)
xlabel('alpha')
ylabel('total cost')
title('Objective Value Progression')

% Plot number of ambulances and helicopters against morality parameter
plot(jerryA(:,1),jerryA(:,2),'k+:','LineWidth',1.25,'MarkerSize',7)
hold on;
plot(jerryH(:,1),jerryH(:,2),'kx--','LineWidth',1,'MarkerSize',7)
legend('Ambulances','Helicopters')
xlabel('alpha')
ylabel('vehicles')
title('Emergency Vehicle Provision')